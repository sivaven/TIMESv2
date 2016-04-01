#include <carlsim.h>
#include <iostream>
#include <cstdlib>
#include <cstdio>
#include <vector>
#include <math.h>
#include <cassert>
#include <limits>

#include <string>
#include <sstream>
#include <fstream>
#include "core/json.h"
#include "core/phenotype-structures.h"
#include "core/MCNeuronSim.h"
#include <ctime>


	MCNeuronSim::MCNeuronSim(const int comp_cnt, int* _connLayout,
			const int scen_count, const int _popSize_neuronCount, double** _parameters){

		scenCount = scen_count;
		compCount = comp_cnt;
		mcScenCount = 0; //for single compartment
		if(compCount>1){
			mcScenCount = 1+			// rheos
					1+					//IR
					(compCount-1)+		//for proprate sims for all dendritic comps.
					(compCount-1); 		// EPSP
		}
		totalGroupCount = scenCount *compCount; // cuz of n compartments
		popSize_neuronCount = _popSize_neuronCount;
		Idur_start_idx = I_start_idx+scenCount ;

		connLayout = _connLayout;

		I = new float* [scenCount];
		Idur = new float* [scenCount];

		excGroup = new int* [scenCount+mcScenCount+1]; // +1 for an extra single compartment single neuron group for epsp scenario
		excMonitor = new SpikeMonitor** [scenCount+mcScenCount];

		for(int i=0;i<scenCount;i++){
			I[i] = new float[popSize_neuronCount];
			Idur[i] = new float[popSize_neuronCount];
		}
		for(int i=0;i<scenCount+mcScenCount;i++){
			excGroup[i] = new int[compCount];
			excMonitor[i] = new SpikeMonitor*[compCount];
		}

		excGroup[scenCount+mcScenCount] = new int[1]; // single compartment neuron in this group
		parameters = _parameters;

		if(compCount>1){
			rheos = new int* [popSize_neuronCount];
			vDefs = new float* [popSize_neuronCount];
			somEpsps = new float* [popSize_neuronCount];
			spikePropRates = new bool* [popSize_neuronCount];
			for(int i=0;i<popSize_neuronCount;i++){
				rheos[i] = new int[compCount];
				vDefs[i] = new float[compCount];
				spikePropRates[i] = new bool[compCount-1];
				somEpsps[i] = new float[compCount-1];
			}
		}

}

	MCNeuronSim::~MCNeuronSim(){
			//timer 6
			int	start_s = clock();
			/*
			 * clearing up!!!
			 */
			for(int i=0;i<scenCount;i++){
				delete I[i];
				delete Idur[i];
			}
			for(int i=0;i<scenCount+mcScenCount;i++){
				delete excGroup[i];
				delete excMonitor[i];
			}
			delete excGroup[scenCount+mcScenCount];

			delete I;
			delete Idur;
			delete excGroup;
			delete excMonitor;
			//delete in;

			if(compCount>1){
						for(int i=0;i<popSize_neuronCount;i++){
							delete rheos[i];
							delete vDefs[i];
							delete spikePropRates[i];
							delete somEpsps[i];
						}
						delete rheos ;
						delete vDefs ;
						delete somEpsps ;
						delete spikePropRates ;
			}




			delete network;

}

	void MCNeuronSim::initNetwork(){
			/**************
		 * [I] construct a CARLsim network on the heap.
		 ***************/
			network = new CARLsim("MCNeuronSim_core", GPU_MODE, SILENT);
	}

	void MCNeuronSim::setupGroups(){
			/*
			 * (A) first setup groups for somatic scenarios! - connect compartments for MC!
			 */
			float G_up, G_dn;
			for(int k = 0; k < scenCount; k++)
			{
				for(int c=0; c<compCount; c++)
				{
					std::string excGroupName = "exc_" + patch::to_string(k) + patch::to_string(c);
					excGroup[k][c] = network->createGroup(excGroupName, popSize_neuronCount, EXCITATORY_NEURON);
					network->setNeuronParameters(excGroup[k][c], 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f);
					//network->setExternalCurrent(excGroup[k][c], 0);
					if(compCount>1){
						G_up = 0;
						G_dn = 0;
						network->setCompartmentParameters(excGroup[k][c], G_up, G_dn);
					}
					excMonitor[k][c] = network->setSpikeMonitor(excGroup[k][c], "/dev/null");

					if(c>0){//connect compartments based on layout
						if(compCount>2 && c==1 && connLayout[c]==connLayout[c+1]){ //meaning 2 dendrites (dend 1 and dend2 ) connecting to the same point
							network->connectCompartments(excGroup[k][c], excGroup[k][connLayout[c]]);
						}else{
							network->connectCompartments(excGroup[k][connLayout[c]], excGroup[k][c]);
						}
					}
				}
			}
			/*
			 * (B) secondly, setup groups for MC scenarios! - connect compartments depending on scenarios as below
			 *  - 1. rheo group : decoupled
			 *  - 2. IR: decoupled
			 *  - 3. spike prop: Coupled
			 *  - 4. syn ampl: Coupled
			 */
			for(int k = scenCount; k < scenCount+mcScenCount; k++)
				{
					for(int c=0; c<compCount; c++)
					{
						std::string excGroupName = "mcc_exc_" + patch::to_string(k) + patch::to_string(c);
						excGroup[k][c] = network->createGroup(excGroupName, popSize_neuronCount, EXCITATORY_NEURON);
						network->setNeuronParameters(excGroup[k][c], 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f);
						if(k>=scenCount+2){// coupled scenarios for mcc
							G_up = 0;
							G_dn = 0;
							network->setCompartmentParameters(excGroup[k][c], G_up, G_dn);
						}
						excMonitor[k][c] = network->setSpikeMonitor(excGroup[k][c], "/dev/null");

						if(k>=scenCount+2){
							if(c>0){//connect compartments based on layout
								if(compCount>2 && c==1 && connLayout[c]==connLayout[c+1]){ //meaning 2 dendrites (dend 1 and dend2 ) connecting to the same point
									network->connectCompartments(excGroup[k][c], excGroup[k][connLayout[c]]);
								}else{
									network->connectCompartments(excGroup[k][connLayout[c]], excGroup[k][c]);
								}
							}
						}
					}
				}
			//single neuron single compartment for EPSP stimulation
			excGroup[scenCount+mcScenCount][0] = network->createGroup("spiker", 1, EXCITATORY_NEURON);
			network->setNeuronParameters(excGroup[scenCount+mcScenCount][0], 121.0f, 0.5343182f,-61.20764f,-40.00114f,
					0.0336908f, 6.5490165f, 35.965454f,-41.31163f, 7.0f);
			const int epspGrpRowStartIdx = scenCount + 2+ (compCount-1);
			for(int kc=0; kc<(compCount-1); kc++){
				network->connect(excGroup[scenCount+mcScenCount][0], excGroup[epspGrpRowStartIdx + kc][kc+1],
											"full", RangeWeight(10.0f), 1.0f);
			}
			/*
			 *  (C) Now, setup network!
			 */
			network->setConductances(true);
			network->setIntegrationMethod(RUNGE_KUTTA4, time_step);
			network->setupNetwork();
		}

	void MCNeuronSim::setupAllNeuronParms(){
			/*
			 * [A] first, setup parms for somatic scenarios!
			 */
			for(unsigned int k = 0; k < scenCount; k++)
				{
					for(unsigned int n = 0; n < popSize_neuronCount; n++)
						{
							setupSingleNeuronParms(k, n, true);
							I[k][n]= parameters[n][I_start_idx+k];
							Idur[k][n]= parameters[n][Idur_start_idx+k];
						}
				}

			/*
			 * [A] second, setup parms for mcc scenarios!
			 *  - remember decoupled mcc scenarios for 1, 2
			 *  - coupled mcc scenarios for 3,... (3+n-1),..
			 */
			//timer 3
			for(unsigned int k = scenCount; k < scenCount+mcScenCount; k++)
				{
					for(unsigned int n = 0; n < popSize_neuronCount; n++)
						{
							if(k>=scenCount+2){// coupled scenarios for mcc
								setupSingleNeuronParms(k, n, true);
							}else{//decoupled mcc scenarios
								setupSingleNeuronParms(k, n, false);
							}
						}
				}
		}

	void MCNeuronSim::setupSingleNeuronParms(int grpRowId, int neurId, bool coupledComp){
			for(unsigned int c = 0; c < compCount; c++) // each neuron has compCount compartments
			{
				network->setIzhikevichParameter(excGroup[grpRowId][c], neurId, "C", parameters[neurId][C_idx[c]]);
				network->setIzhikevichParameter(excGroup[grpRowId][c], neurId, "k", parameters[neurId][k_idx[c]]);
				network->setIzhikevichParameter(excGroup[grpRowId][c], neurId, "vr", parameters[neurId][vr_idx]);
				network->setIzhikevichParameter(excGroup[grpRowId][c], neurId, "vt", parameters[neurId][vr_idx] + parameters[neurId][vt_idx[c]]);
				network->setIzhikevichParameter(excGroup[grpRowId][c], neurId, "a", parameters[neurId][a_idx[c]]);
				network->setIzhikevichParameter(excGroup[grpRowId][c], neurId, "b", parameters[neurId][b_idx[c]]);
				if(c==0){//somatic
					network->setIzhikevichParameter(excGroup[grpRowId][c], neurId, "vpeak", parameters[neurId][vr_idx] + parameters[neurId][vpeak_idx[0]]);
				}else{
					//dendritic vpeak is relative to somatic vpeak
					network->setIzhikevichParameter(excGroup[grpRowId][c], neurId, "vpeak",
													(parameters[neurId][vr_idx] + parameters[neurId][vpeak_idx[0]]) - parameters[neurId][vpeak_idx[c]]);
				}
				network->setIzhikevichParameter(excGroup[grpRowId][c], neurId, "c", parameters[neurId][vr_idx]+ parameters[neurId][vmin_idx[c]]);
				network->setIzhikevichParameter(excGroup[grpRowId][c], neurId, "d", parameters[neurId][d_idx[c]]);

				if(coupledComp){
					if(c>0){
						double G = parameters[neurId][G_idx[c-1]];
						double P = parameters[neurId][P_idx[c-1]];
						float fwd = G * P;
						float bwd = G * (1-P);
						/*
						 * generally, fwd is carlsim 'down', bwd is carlsim 'up' for the purpose of coupling constant assignment, but,
						 * when there is a dendrite 'below' soma: ****cases 3c2 and 4c2***
						 * up and down are reversed.
						 */
						if(compCount>2 && c==1 && connLayout[c]==connLayout[c+1]){ //meaning 2 dendrites (dend 1 and dend2 ) connecting to the same point
							network->setCouplingConstant(excGroup[grpRowId][connLayout[c]], neurId, "down", bwd);
							network->setCouplingConstant(excGroup[grpRowId][c], neurId, "up", fwd);
						}else{
							network->setCouplingConstant(excGroup[grpRowId][c], neurId, "down", fwd);
							network->setCouplingConstant(excGroup[grpRowId][connLayout[c]], neurId, "up", bwd);
						}
					}
				}
			}
		}

	void MCNeuronSim::setupIandRunNetwork(){
			for(int k = 0; k < scenCount; k++)
				{
					network->setExternalCurrent(excGroup[k][0], *I[k]);			// external current scenarios are only for somatic compartment
					excMonitor[k][0]->startRecording();
				}

			network->runNetwork(0, Idur[0][0]); //remember, Idur has repetitive values (same for all neurons within a group)
			network->setExternalCurrent(excGroup[0][0], 0);
			//network->runNetwork(0, 100);

			for(int k = 1; k < scenCount; k++)
			 {
				network->runNetwork(0, Idur[k][0]-Idur[k-1][0]); // assuming durations are in asc order
				network->setExternalCurrent(excGroup[k][0], 0);
			 }
			/* if(runTime>Idur[scenCount-1][0]){
				 network->runNetwork(0, runTime - Idur[scenCount-1][0]); // complete 1s runtime, if required
			 }*/
		}

	void MCNeuronSim::writePhenotype(std::ostream &outputStream){
			std::vector< std::vector<int> > spikeTimes[scenCount];
			for(int k = 0; k < scenCount; k++)
				{
					excMonitor[k][0]->stopRecording();
					spikeTimes[k] = excMonitor[k][0]->getSpikeVector2D();
				}

			for(unsigned int i = 0; i < popSize_neuronCount; i++) {
				Json::Value wrapper;
				Json::Value somaPatterns(Json::arrayValue);

				/*
				 * [1] write somatic scenarios!
				 */
				for(unsigned int k = 0; k < scenCount; k++){
					int* spike_times = &spikeTimes[k][i][0];
					int stl = spikeTimes[k][i].size();
					//	float* group_voltage = network->retrieveGroupVoltages(g);
					float vTrace[0];
					int vtl = 0;
					SpikePatternJson* sp = new SpikePatternJson(I[k][i], Idur[k][i], 0.1f, spike_times, stl, vTrace, vtl);
					Json::Value spikePattern = sp->retrieveJson();
					somaPatterns.append(spikePattern);
					delete sp;
				}
				wrapper["soma_patterns"] = somaPatterns;

				/*
				 * [2] write mcc scenarios!
				 */
				if(compCount>1){
					MultiCompDataJson* mcd = new MultiCompDataJson(rheos[i], vDefs[i], spikePropRates[i], somEpsps[i], compCount);
					Json::Value mcData = mcd->retrieveJson();
					wrapper["multi_comp_sim"] = mcData;
				}

				std::string fileNameStr = std::string("results/")+patch::to_string(i)+std::string("_phenotype");
				const char *fileName = fileNameStr.c_str();
				std::ofstream file(fileName);
				file << wrapper << std::endl;
				file.close();

				outputStream << fileNameStr << std::endl;
			}
		}

	void MCNeuronSim::writePhenotype(){
				std::vector< std::vector<int> > spikeTimes[scenCount];
				for(int k = 0; k < scenCount; k++)
					{
						excMonitor[k][0]->stopRecording();
						spikeTimes[k] = excMonitor[k][0]->getSpikeVector2D();
					}

				for(unsigned int i = 0; i < popSize_neuronCount; i++) {
					Json::Value wrapper;
					Json::Value somaPatterns(Json::arrayValue);

					/*
					 * [1] write somatic scenarios!
					 */
					for(unsigned int k = 0; k < scenCount; k++){
						int* spike_times = &spikeTimes[k][i][0];
						int stl = spikeTimes[k][i].size();
						//	float* group_voltage = network->retrieveGroupVoltages(g);
						float vTrace[0];
						int vtl = 0;
						SpikePatternJson* sp = new SpikePatternJson(I[k][i], Idur[k][i], 0.1f, spike_times, stl, vTrace, vtl);
						Json::Value spikePattern = sp->retrieveJson();
						somaPatterns.append(spikePattern);
						delete sp;
					}
					wrapper["soma_patterns"] = somaPatterns;

					/*
					 * [2] write mcc scenarios!
					 */
					if(compCount>1){
						MultiCompDataJson* mcd = new MultiCompDataJson(rheos[i], vDefs[i], spikePropRates[i], somEpsps[i], compCount);
						Json::Value mcData = mcd->retrieveJson();
						wrapper["multi_comp_sim"] = mcData;
					}

					std::string fileNameStr = std::string("results/")+patch::to_string(i)+std::string("_phenotype");
					const char *fileName = fileNameStr.c_str();
					std::ofstream file(fileName);
					file << wrapper << std::endl;
					file.close();
				}
			}
	 /* Determines the excitability of compartments as rheo currents
		 *  - rheo: minimum depolarizing current required to elicit spikes
		 *  - measured for isolated compartments
		 * 	- dendritic rheo is returned only if it is less than somatic, which is not valid,
		 * 		and consequently an error will be added to ECJ fitness, depending on the rheo difference bw soma and dendrite.
		 * 	- the loop (with increasing current) is stopped once all SOMATIC rheos have been identified
		 * 		- therefore, for an undetermined dendritic compartment, its rheo is either higher than the already found-out rheo of soma (valid case)
		 *		- or, this undetermined dendritic compartment is passive (no rheo current could be found at all), which is also a valid case
		 *
	 *		- uses ramp current to save sim time: ***NEW***
		 */
	void MCNeuronSim::determineRheo(int iMin, int iMax, int iStep, int iDur){
			/*int runDur = Idur[scenCount-1][0];
				if(runTime>Idur[scenCount-1][0]){
					 runDur = runTime;
				 }
			 */

				const int grpRowIdx = scenCount+ 0;

				bool rheoFound[popSize_neuronCount][compCount];
				// init with default
				for(int i=0;i<popSize_neuronCount;i++){
					for(int j=0;j<compCount;j++){
						rheos[i][j]=99999;
						rheoFound[i][j]=false;
					}
				}

				for(unsigned int I=iMin; I<iMax; I+=iStep){
					for(int c=0;c<compCount;c++){
						network->setExternalCurrent(excGroup[grpRowIdx][c], I);
						excMonitor[grpRowIdx][c]->startRecording();
					}

					//run and retrieve spikevectors

					network->runNetwork(0, iDur);
					//runDur += iDur;

					std::vector< std::vector<int> > spikeTimes[compCount];
					for(int c=0;c<compCount;c++){
						excMonitor[grpRowIdx][c]->stopRecording();
						spikeTimes[c] = excMonitor[grpRowIdx][c]->getSpikeVector2D();
						excMonitor[grpRowIdx][c]->clear();
						/*
						 * reset necessary? shouldnt be!

						network->setExternalCurrent(excGroup[grpRowIdx][c], 0);
						network->runNetwork(0, 100);
						//runDur += 100;
						 *
						 * */
					}

					//set rheobase
					for(int i=0;i<popSize_neuronCount;i++){
						for(int c=0;c<compCount;c++){
							if(!rheoFound[i][c]){
								if(spikeTimes[c][i].size()>0){
									//std::cout<<"comp. "<<c<<"\n";
									//std::cout<<"current. "<<I<<"\n";
									//std::cout<<"st size. "<<spikeTimes[c][i].size()<<"\n";
									//std::cout<<"spike times 0. "<<(spikeTimes[c][i][0]-(runDur-iDur))<<"\n";
									//std::cout<<"sim time. "<<runDur<<"\n";
									rheoFound[i][c]=true;
									rheos[i][c]=I;
								}
							}
						}
					}

					//check for termination
					bool allSomaticRheoFound = true;
					for(int i=0;i<popSize_neuronCount;i++){
						if(!rheoFound[i][0]){
							allSomaticRheoFound = false;
						}
					}
					if(allSomaticRheoFound){
						break;
					}
				}
			}

	void MCNeuronSim::measureVoltageDeflection(int I, int I_dur,  int V_at_T){
		//	int runDur = Idur[scenCount-1][0];
			const int grpRowIdx = scenCount+ 1;

			for(int c=0;c<compCount;c++){
				network->setExternalCurrent(excGroup[grpRowIdx][c], I);
				//excMonitor[grpRowIdx][c]->startRecording();
				for(int i=0;i<popSize_neuronCount;i++){
					std::string fileName = "V_"+ patch::to_string(i) + "_" + patch::to_string(c);
					network->recordVoltage(excGroup[grpRowIdx][c], i, fileName);
				}
			}

			network->runNetwork(0, I_dur);

			//now, read Vs from the written file!
			for(int c=0;c<compCount;c++){
				for(int i=0;i<popSize_neuronCount;i++){
					std::string fileName = "V_"+ patch::to_string(i) + "_" + patch::to_string(c);
					float* vTrace = readVtrace(fileName, I_dur);
					vDefs[i][c]=vTrace[V_at_T-1];
					delete vTrace;
				}
			}
		}

	float* MCNeuronSim::readVtrace(std::string fileName, int nLines){
			float* vTrace = new float[nLines];

			std::ifstream infile(fileName.c_str());

			std::string file_line;
			std::string delimiter = ";";
			int i=0;
			while (std::getline(infile, file_line))
			{
				if(i>=nLines){
					break;
					//std::cout<<"More lines read than expected from "+patch::to_string(fileName)+"--nlines read:"+patch::to_string(i+1);
				}
			    std::istringstream iss1(file_line);
			    std::string line;
			    iss1 >> line;

			    //tokenize and convert to float
			    std::string token =line.substr(0, line.find(delimiter));
				std::istringstream iss2(token);
				iss2 >> vTrace[i++];
				//std::cout<<vTrace[i-1]<<"\n";
			}
			std::remove(fileName.c_str());
			return vTrace;
		}

	void MCNeuronSim::determinePropRate(int iMin, int iMax, int iStep, int iDur){
		//	int runDur = Idur[scenCount-1][0];
				const int grpRowStartIdx = scenCount+ 2;

				bool thresholdCurrentFound[popSize_neuronCount][compCount-1];
				// init with default
				for(int i=0;i<popSize_neuronCount;i++){
					for(int j=0;j<compCount-1;j++){
						spikePropRates[i][j]=false;
						thresholdCurrentFound[i][j]=false;
					}
				}


				for(unsigned int I=iMin; I<iMax; I+=iStep){
					for(int kc=1;kc<compCount;kc++){//ofset for grprowstartidx; also compartment index
						network->setExternalCurrent(excGroup[grpRowStartIdx+(kc-1)][kc], I);
						excMonitor[grpRowStartIdx+(kc-1)][kc]->startRecording(); //group where I is injected
						excMonitor[grpRowStartIdx+(kc-1)][connLayout[kc]]->startRecording(); //group where prop rate checked
					}
					network->runNetwork(0, iDur);
					//runDur+= iDur;
					std::vector< std::vector<int> > spikeTimes_siteOfI[compCount-1];
					std::vector< std::vector<int> > spikeTimes_siteOfCheck[compCount-1];
					for(int kc=1;kc<compCount;kc++){
						excMonitor[grpRowStartIdx+(kc-1)][kc]->stopRecording();
						excMonitor[grpRowStartIdx+(kc-1)][connLayout[kc]]->stopRecording();
						spikeTimes_siteOfI[kc-1] = excMonitor[grpRowStartIdx+(kc-1)][kc]->getSpikeVector2D();
						spikeTimes_siteOfCheck[kc-1] = excMonitor[grpRowStartIdx+(kc-1)][connLayout[kc]]->getSpikeVector2D();
						excMonitor[grpRowStartIdx+(kc-1)][kc]->clear();
						excMonitor[grpRowStartIdx+(kc-1)][connLayout[kc]]->clear();
					}

					//set threshold and other
					for(int i=0;i<popSize_neuronCount;i++){
						for(int kc=0;kc<compCount-1;kc++){
							if(!thresholdCurrentFound[i][kc]){
								if(spikeTimes_siteOfI[kc][i].size()>0){
									//std::cout<<"comp. site. of I. "<<(kc+1)<<"\n";
									//std::cout<<"current. "<<I<<"\n";
									//std::cout<<"st size. "<<spikeTimes[c][i].size()<<"\n";
									//std::cout<<"spike times 0. "<<(spikeTimes_siteOfI[kc][i][0]-(runDur))<<"\n";
									//std::cout<<"sim time. "<<runDur<<"\n";
									thresholdCurrentFound[i][kc]=true;
									/*
									 * here look up the down compartment spike count and set!
									 */
									if(spikeTimes_siteOfCheck[kc][i].size()>0){
										spikePropRates[i][kc]=true;
									}
								}
							}
						}
					}

					//check for termination
					bool allThresholdFound = true;
					for(int i=0;i<popSize_neuronCount;i++){
						for(int j=0;j<compCount-1;j++){
							if(!thresholdCurrentFound[i][j]){
								allThresholdFound = false;
							}
						}
					}
					if(allThresholdFound){
						break;
					}
			}
		}

	void MCNeuronSim::measureSomaticEPSP(int I_dur){
			const int epspGrpRowStartIdx = scenCount + 2+ (compCount-1);
			const float I_for_epsp = 188;
			network->setExternalCurrent(excGroup[scenCount+mcScenCount][0], I_for_epsp);

			for(int kc=0; kc<(compCount-1); kc++){
				for(int i=0;i<popSize_neuronCount;i++){
						std::string fileName = "V_"+ patch::to_string(i) + "_" + patch::to_string(kc+1)+"_";
						network->recordVoltage(excGroup[epspGrpRowStartIdx + kc][0], i, fileName); //always record frm soma - indx = 0
				}
			}

			network->runNetwork(0, I_dur);

			//now, read Vs from the written file!
			for(int kc=0; kc<(compCount-1); kc++){
				for(int i=0;i<popSize_neuronCount;i++){
					std::string fileName = "V_"+ patch::to_string(i) + "_" + patch::to_string(kc+1)+"_";
					float* vTrace = readVtrace(fileName, I_dur);

					float maxVoltage = -1000;
					for(int ii=0;ii<I_dur;ii++){
						if(vTrace[ii]>maxVoltage){
							maxVoltage = vTrace[ii];
						}
					}
					somEpsps[i][kc]=maxVoltage;
					delete vTrace;
				}
			}
	}

	void MCNeuronSim::writeTime(std::string item, double timeInNanoSec){
		double timeInMs = (timeInNanoSec)/double(CLOCKS_PER_SEC)*1000;
		FILE *fp;
		fp = fopen("Info.txt", "a");
		fprintf(fp, item.c_str());
		fprintf(fp, " :\t");
		fprintf(fp, (patch::to_string(timeInMs)).c_str());
		fprintf(fp,"\n");
		fclose(fp);
	}
