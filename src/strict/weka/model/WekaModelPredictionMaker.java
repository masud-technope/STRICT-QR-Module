package strict.weka.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;
import strict.utility.ContentWriter;
import strict.utility.SelectedBugs;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.output.prediction.PlainText;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.Logistic;
import weka.classifiers.meta.Bagging;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.meta.Stacking;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.SimpleCart;
import weka.core.Instances;
import weka.core.Range;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.Randomize;

public class WekaModelPredictionMaker {

	String repoName;
	String arffFile;
	double maxAccuracy = 0;
	double bestKFolds = 0;
	String predictionResult;
	String predictionFile;

	public WekaModelPredictionMaker(String repoName, String arffFile) {
		this.repoName = repoName;
		this.arffFile = arffFile;
		this.predictionResult = new String();
	}
	
	public void setPredictionFile(String filePath) {
		this.predictionFile = filePath;
	}

	protected Instances loadDataFromARFF() {
		Instances data = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(this.arffFile));
			data = new Instances(reader);
			reader.close();
		} catch (Exception exc) {
			// handle the exception
		}
		return data;
	}

	protected Instances removeUnexpectedColumns(Instances data) {
		Instances refinedData = null;
		try {
			Remove removeFilter = new Remove();
			removeFilter.setAttributeIndices("1");
			removeFilter.setInputFormat(data);
			refinedData = Filter.useFilter(data, removeFilter);
		} catch (Exception exc) {
			// handle the exception
			exc.printStackTrace();
		}
		return refinedData;
	}

	protected J48 getJ48Classifier(int seed) {
		J48 j48 = null;
		try {
			j48 = new J48();
			j48.setConfidenceFactor(0.25f);
			j48.setMinNumObj(2);
			j48.setSeed(seed);
		} catch (Exception exc) {
			// handle the exception
		}
		return j48;
	}

	protected SimpleCart getCARTClassifier(int seed) {
		SimpleCart cartClassifier = new SimpleCart();
		cartClassifier.setSeed(seed);
		cartClassifier.setNumFoldsPruning(5);
		cartClassifier.setMinNumObj(2.0);
		cartClassifier.setSizePer(1.0);
		return cartClassifier;
	}

	protected Logistic getLogisticRegressionClassifier() {
		Logistic logistic = new Logistic();
		logistic.setMaxIts(-1);
		logistic.setRidge(1.0E-8);
		// logistic.setNumDecimalPlaces(4);
		return logistic;
	}

	protected LibSVM getSVM(int seed) {
		LibSVM svm = new LibSVM();
		// svm.setSeed(seed);
		return svm;
	}

	protected Bagging getBaggingClassifier(int seed) {
		Bagging baggingClassifier = null;
		try {
			baggingClassifier = new Bagging();
			baggingClassifier.setSeed(seed);
			Classifier classifier = getJ48Classifier(seed);
			// Classifier classifier = getREPTreeClassifier(seed);
			baggingClassifier.setNumIterations(5);
			baggingClassifier.setClassifier(classifier);
		} catch (Exception exc) {
			// handle the exception
		}
		return baggingClassifier;
	}

	protected NaiveBayes getNaiveBayes() {
		NaiveBayes nbayes = new NaiveBayes();
		// nbayes.setNumDecimalPlaces(2);
		return nbayes;
	}

	protected Stacking getStackingClassifier(int seed, int numFolds) {
		Stacking stackingClassifier = null;
		try {
			stackingClassifier = new Stacking();
			Classifier nb = getNaiveBayes();
			Classifier j48 = getJ48Classifier(seed);
			Classifier[] classifiers = new Classifier[2];
			classifiers[0] = nb;
			classifiers[1] = j48;
			stackingClassifier.setClassifiers(classifiers);
			// Classifier metaClassifier = getLogisticRegressionClassifier();
			Classifier metaClassifier = getLogisticRegressionClassifier();
			stackingClassifier.setMetaClassifier(metaClassifier);
			stackingClassifier.setNumFolds(numFolds);
			stackingClassifier.setSeed(seed);
		} catch (Exception exc) {
			// handle the exception
		}
		return stackingClassifier;
	}

	protected Randomize getRandomizeFilter(int seed) {
		Randomize randomizeFilter = new Randomize();
		randomizeFilter.setRandomSeed(seed);
		return randomizeFilter;
	}

	protected Remove getRemoveFilter(int removeIndex) {
		Remove removeFilter = new Remove();
		removeFilter.setAttributeIndices(Integer.toString(removeIndex));
		return removeFilter;
	}

	protected FilteredClassifier getFilteredClassifier(int seed, String algoKey) {
		FilteredClassifier fc = new FilteredClassifier();
		MultiFilter multiFilter = new MultiFilter();
		Filter[] filters = new Filter[2];
		filters[0] = getRemoveFilter(1);
		filters[1] = getRandomizeFilter(seed);
		multiFilter.setFilters(filters);
		fc.setFilter(multiFilter);
		fc.setClassifier(getClassifier(seed, algoKey));
		return fc;
	}

	protected Classifier getClassifier(int seed, String algoKey) {
		Classifier classifier = null;
		switch (algoKey) {
		case "J48":
			classifier = getJ48Classifier(seed);
			break;
		case "LR":
			classifier = getLogisticRegressionClassifier();
			break;
		case "CART":
			classifier = getCARTClassifier(seed);
			break;
		case "BAG":
			classifier = getBaggingClassifier(seed);
			break;
		case "NB":
			classifier = getNaiveBayes();
			break;
		case "ST":
			classifier = getStackingClassifier(seed, 10);
			break;
		case "SVM":
			classifier = getSVM(seed);
			break;
		}
		return classifier;
	}

	public String determineBestClassifications(int seed, String algoKey) {

		Instances loadedData = loadDataFromARFF();
		// removing the ID
		// loadedData = removeUnexpectedColumns(loadedData);
		
		// setting the class Index
		loadedData.setClassIndex(loadedData.numAttributes() - 1);

		int TOTAL_INSTANCES = loadedData.numInstances();

		int totalBugCount = SelectedBugs.loadSelectedBugs(repoName).size();

		for (int K = 2; K <= TOTAL_INSTANCES; K += 3 ) {

			try { 
				// basic Random Forest
				Classifier classifier = getFilteredClassifier(seed, algoKey);
				Evaluation eval = new Evaluation(loadedData);

				
				StringBuffer predictionSB = new StringBuffer();
				Range attributesToShow = new Range("1");
				Boolean outputDistributions = new Boolean(true);

				PlainText predictionOutput = new PlainText();
				predictionOutput.setBuffer(predictionSB);
				predictionOutput.setOutputDistribution(true);
				

				// you need to set the attribute index here as well. We are
				// collecting the instance ID here
				predictionOutput.setAttributes("1");

				eval.crossValidateModel(classifier, loadedData, K, new Random(1), predictionSB, attributesToShow,
						outputDistributions);

				double acc = eval.pctCorrect();
				if (acc > maxAccuracy) {
					maxAccuracy = acc;
					bestKFolds = K;
					predictionResult = predictionSB.toString();
				}
				System.gc();
				
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}

		System.out.println(repoName + "\t" + bestKFolds + "\t" + maxAccuracy);
		// System.out.println(predictionResult);

		return predictionResult;
	}

	public void saveEvaluations(String evaluationResult) {
		ContentWriter.writeContent(this.predictionFile, evaluationResult);
	}
}
