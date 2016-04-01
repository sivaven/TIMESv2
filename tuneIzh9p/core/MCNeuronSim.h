#include <carlsim.h>


class MCNeuronSim{
	public:
		static int time_step;
		static int k_idx[4];
		static int a_idx[4];
		static int b_idx[4];
		static int d_idx[4];
		static int C_idx[4];
		static int vr_idx;
		static int vt_idx[4];
		static int vmin_idx[4];
		static int vpeak_idx[4];

		static int G_idx[3] ;
		static int P_idx[3] ;
		static int W_idx[3] ;

		static int I_start_idx ;
		// Simulation time (each must be at least 1s due to bug in SpikeMonitor)
		//static int runTime ;

	private:
		CARLsim* network;
		double** parameters; //for debug mode; user defined parm
		int popSize_neuronCount;

		int scenCount; // # of scenarios for somatic constraints
		int mcScenCount; // currently, a constant 4 for all MC, 0 for SC!
		int compCount;
		int totalGroupCount;
		int Idur_start_idx;
		float** I;		// a slightly varied current appended to every parameter instance
		float** Idur;   // in reality, this is a 1d array, but it's appended to every parameter instance

		int** excGroup;
		SpikeMonitor*** excMonitor;

		int** rheos;
		float** vDefs;
		float** somEpsps;
		bool** spikePropRates;

		int* connLayout;

	public:
		MCNeuronSim(const int comp_cnt, int* _connLayout,
				const int scen_count, const int _popSize_neuronCount, double** _parameters);

		~MCNeuronSim();

		void initNetwork();
		void setupGroups();
		void setupAllNeuronParms();
		void setupSingleNeuronParms(int grpRowId, int neurId, bool coupledComp);
		void setupIandRunNetwork();
		void writePhenotype();
		void writePhenotype(std::ostream &outputStream);

		void determineRheo(int iMin, int iMax, int iStep, int iDur);
		void measureVoltageDeflection(int I, int Idur, int V_at_T);
		float* readVtrace(std::string fileName, int nLines);
		void measureSomaticEPSP(int Idur);
		void determinePropRate(int iMin, int iMax, int iStep, int iDur);

		void writeTime(std::string item, double time);
};
