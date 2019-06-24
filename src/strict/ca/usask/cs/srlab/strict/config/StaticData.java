package strict.ca.usask.cs.srlab.strict.config;

public class StaticData {

	// public final attributes
	// public static String HOME_DIR = "F:/MyWorks/Thesis Works/PhDThesisTool";
	public static String HOME_DIR = "C:/MyWorks/PhDThesisTool";

	public static String INDEX_FOLDER = HOME_DIR + "/lucene/index-method";
	public static String CORPUS_FOLDER = HOME_DIR + "/corpus/norm-method";

	public static String STOPWORD_DIR = HOME_DIR+ "/pp-data";
	public static String SAMURAI_DIR = HOME_DIR + "/samurai-data";
	public static String MAX_ENT_MODELS_DIR = HOME_DIR+"/models";

	public static double SIGNIFICANCE_THRESHOLD = 0.0001;
	public final static int WINDOW_SIZE = 2;
	public static int MAX_QUERY_LEN = 1024;

	public static double INITIAL_TERM_WEIGHT = 0.25;
	public static double KEYWORD_RATIO = 0.33;
	public static int KCORE_SIZE = 2;

	// public static double alpha =1;// 0.45309403507098156;
	// public static double beta = 1;//0.8374424351745824;
	// public static double gamma =1;// 0.3753504417175002;

	public static double alpha = 0.45309403507098156;
	public static double beta = 0.8374424351745824;
	public static double gamma = 0.3753504417175002;

	public static double REDUCTION_GAIN_TH = 0.50;

}
