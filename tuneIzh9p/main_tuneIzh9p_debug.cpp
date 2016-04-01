/*! \brief
 *
 */
#include <PTI.h>
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
#include "core/json.h"
#include "core/MCNeuronSim.h"
#include <ctime>

using namespace std;
using namespace CARLsim_PTI;

/*
 * initialize const indices! 1 comp
 */
/*
int MCNeuronSim::k_idx[] = {0};int MCNeuronSim::a_idx[] = {1};int MCNeuronSim::b_idx[] ={2};int MCNeuronSim::d_idx[] = {3};int MCNeuronSim::C_idx[]= {4};
int MCNeuronSim::vr_idx = 5;int MCNeuronSim::vt_idx[]={6};int MCNeuronSim::vmin_idx[]={7};int MCNeuronSim::vpeak_idx[] ={8};
int MCNeuronSim::G_idx[] = {-1};int MCNeuronSim::P_idx[] = {-1};int MCNeuronSim::W_idx[] = {-1};int MCNeuronSim::I_start_idx =9;
*/

/*
 * initialize const indices! 2 comp
 */
/* int MCNeuronSim::k_idx[] = {0, 1};int MCNeuronSim::a_idx[] = {2,3};int MCNeuronSim::b_idx[] ={4,5};int MCNeuronSim::d_idx[] = {6,7};
int MCNeuronSim::C_idx[]= {8,9};int MCNeuronSim::vr_idx = 10;int MCNeuronSim::vt_idx[]={11,12};int MCNeuronSim::vmin_idx[]={13,14};int MCNeuronSim::vpeak_idx[] ={15,16};
int MCNeuronSim::G_idx[] = {17};int MCNeuronSim::P_idx[] = {18};int MCNeuronSim::W_idx[] = {19};int MCNeuronSim::I_start_idx =20;
*/
/*
 * initialize const indices! 3 comp
 */
/*
 int MCNeuronSim::k_idx[] = {0, 1, 2};int MCNeuronSim::a_idx[] = {3,4,5};int MCNeuronSim::b_idx[] ={6,7,8};int MCNeuronSim::d_idx[] = {9,10,11};
int MCNeuronSim::C_idx[]= {12,13,14};int MCNeuronSim::vr_idx = 15;int MCNeuronSim::vt_idx[]={16,17,18};int MCNeuronSim::vmin_idx[]={19,20,21};int MCNeuronSim::vpeak_idx[] ={22,23,24};
int MCNeuronSim::G_idx[] = {25,26};int MCNeuronSim::P_idx[] = {27,28};int MCNeuronSim::W_idx[] = {29,30};int MCNeuronSim::I_start_idx =31;
*/
/*
 * initialize const indices! 4 comp
 */

 int MCNeuronSim::k_idx[] = {0, 1, 2, 3};int MCNeuronSim::a_idx[] = {4, 5, 6, 7};int MCNeuronSim::b_idx[] ={8, 9, 10, 11};int MCNeuronSim::d_idx[] = {12, 13, 14, 15};
int MCNeuronSim::C_idx[]= {16, 17, 18, 19};int MCNeuronSim::vr_idx = 20;int MCNeuronSim::vt_idx[]={21, 22, 23, 24};int MCNeuronSim::vmin_idx[]={25, 26, 27, 28};int MCNeuronSim::vpeak_idx[] ={29, 30, 31, 32};
int MCNeuronSim::G_idx[] = {33, 34, 35};int MCNeuronSim::P_idx[] = {36, 37, 38};int MCNeuronSim::W_idx[] = {39, 40, 41};int MCNeuronSim::I_start_idx =42;

/*
 * common
 */
//int MCNeuronSim::runTime = 1000;
int MCNeuronSim::time_step =100;

int main(int argc, char* argv[]) {

	const int compCnt = 4;
	const int conn_layout[compCnt] = {0,0,0,2};
	const int scenCount =1;
	const int popSize_neuronCount = 1;
	const int N_NEURONS = 1;
	const int N_PARMS = (9*compCnt-(compCnt-1)) + (3*(compCnt-1)) + (2*scenCount);

	double parms[N_PARMS]	=
	/*
	 * sample 1c
	 */
			//{0.5343182,0.0336908,6.5490165,7,121,-61.20764,21.2065,19.89601,97.173094,188,500};
	/*
	 * sample 2cs
	 */
			//{1.0677531, 1, 0.0031239379, 0, -23.12086, 0, 78.0, 1, 29.0, 1,		-74.06058,		58.014275, 58, 31.258423, 31, 90.07723, 90, 250, 0.2, 1,			380.0, 999};
			//{2.611654, 5.872848, 0.005242629, 0.011855498, -17.572365, 24.44083, 91.0, 96.0, 378.0, 268.0, -59.181076, 17.376963, 15.868967, 8.01981, 8.218209, 102.05554, 15.943001, 79.0, 0.13422246, 0.78004766, 216.0, 500.0};
			//{0.46714354, 0.16651097, 0.0014378154, 0.0011669829, -17.307766, 6.527672, 80.0, 3.0, 239.0, 209.0, -59.67108, 22.571863, 23.961445, 8.924491, 8.343913, 94.141914, 16.195194, 120.0, 0.100301474, 0.7480465, 92.0, 500.0};

	/* sample 3c2
	 *
	 */
			/*{3.9752178,	1.2633001,	1.2054616,	0.001425919,	1.0446243,	0.45259273,	-11.811985,	11.087972,	3.1462605,	106,	62,	62,	551,	555,	353,
			-59.66081,	6.49226,	36.828227,	41.790073,	5.343133,	29.681926,	25.404863,	94.587778,	7.290089,	8.608138,	26,	43,	0.8004273,	0.69688535,
			12.026643,	7.7694597,	590,	800};*/

	/*
	 * sample 4c2
	 */
			{2.3330991,	1.109572,	1.1705916,	2.2577047,0.0021015,	0.29814243,	0.2477681,	0.32122386,-0.41361538,	-4.385603,	3.3198094,	0.14995363,
			109,	21,	24,	69,550,	225,	367,	425,-59.101414	,	8.672528,	22.543394,	14.80312,	33.96352,		5.878201,	18.75742,	13.02459,	20.552494,84.088394,	3.513126,	4.712684,	11.783566,
			166,	33,	56,	0.29601815,	0.86049455,	0.90131694,	9.308023,	7.7694597,	5.7076874,592	, 800	};

	double** _passParms;
	_passParms = new double*[N_NEURONS];
	_passParms[0] = new double[N_PARMS];
	for(int i=0;i<N_PARMS;i++){
		_passParms[0][i] = parms[i];
	}
	int* _connLayout = new int[compCnt];
	for(int i=0;i<compCnt;i++){
		_connLayout[i] = conn_layout[i];
	}

	MCNeuronSim* mc = new MCNeuronSim(compCnt, _connLayout, scenCount, popSize_neuronCount,_passParms);

	int start_s=clock();
		mc->initNetwork();
	int stop_s=clock();
	mc->writeTime("instantiation", stop_s-start_s);

	start_s=clock();
		mc->setupGroups();
		mc->setupAllNeuronParms();
	stop_s=clock();
	mc->writeTime("setup Groups and Parms", stop_s-start_s);

	start_s=clock();
		mc->setupIandRunNetwork();
	stop_s=clock();
	mc->writeTime("I and run Network", stop_s-start_s);

	if(compCnt > 1){
		start_s=clock();
				mc->determineRheo(10, 1000, 1, 1);
			stop_s=clock();
			mc->writeTime("determine ramp rheo", stop_s-start_s);

			start_s=clock();
				mc->measureVoltageDeflection(-100, 500, 450);
			stop_s=clock();
			mc->writeTime("measure vDef", stop_s-start_s);

			start_s=clock();
				mc->determinePropRate(1000,2000,250,100);
			stop_s=clock();
			mc->writeTime("determine prop rate", stop_s-start_s);

			start_s=clock();
				mc->measureSomaticEPSP(50); // pregroup neuron spike at 34 ms!
			stop_s=clock();
			mc->writeTime("measure epsp", stop_s-start_s);
	}

	start_s=clock();
		mc->writePhenotype();
	stop_s=clock();
	mc->writeTime("write-phenotype", stop_s-start_s);

	delete mc;
}





