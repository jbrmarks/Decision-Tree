import java.util.*;
import java.io.*;

////////////////////////////////////////////////////////////////////////////
//                                                                       
// Code for HW1, Problem 2
//               Inducing Decision Trees
//               CS540 (Shavlik), Spring 2013
//
// Modifications to supplied code made by Joe Marks
//                                                                        
////////////////////////////////////////////////////////////////////////////

/* BuildAndTestDecisionTree.java 

   Copyright 2008, 2011, 2013 by Jude Shavlik and Nick Bridle.
   May be freely used for non-profit educational purposes.

   Additional code added by Joe Marks

   To run after compiling, type:

     java BuildAndTestDecisionTree <trainsetFilename> <testsetFilename>

   Eg,

     java BuildAndTestDecisionTree train-hepatitis.data test-hepatitis.data

   where <trainsetFilename> and <testsetFilename> are the 
   input files of examples.

   Notes:  

           Please keep your program to this single file. 

           All that is required is that you keep the name of the
          BuildAndTestDecisionTree class and don't change the 
          calling convention for its main function.  

           There is no need to worry about "error detection" when reading
           data files.
           We'll be responsible for that.  HOWEVER, DO BE AWARE THAT
           WE WILL USE ONE OR MORE DIFFERENT DATASETS DURING TESTING,
           SO DON'T  WRITE CODE THAT IS SPECIFIC TO THE "HEPATITIS" 
           DATASET.  (As  stated above, you may assume that our additional 
           datasets are properly formatted in the 
           style used for the hepatitis data.)

           A weakness of our design is that the category and feature
           names are defined in BOTH the train and test files.  These
           names MUST match, though this isn't checked.  However,
           we'll live with the weakness because it reduces complexity
           overall (note: you can use the SAME filename for both the
           train and the test set, as a debugging method; you should
           get ALL the test examples correct in this case, since we are
	   not "pruning" decision trees to avoid overfitting the training data
	    - but be sure you understand Problem 1's method for pruning 
	   decision trees). 
 */

public class BuildAndTestDecisionTree
{
	/**
	 * "Main" reads in the names of the files we want to use, then reads 
	 * in their examples.  It then creates a decision tree based on the
	 * training examples read in and prints said tree.  Then tests 
	 * testing examples read in on the tree, printing statistics about its
	 * accuracy.
	 * 
	 * @param args
	 * 	args[0] : training set filename
	 *  args[1] : testing set filename
	 */
	public static void main(String[] args)
	{   
		if (args.length != 2)
		{
			System.err.println("You must call BuildAndTestDecisionTree as " + 
					"follows:\n\njava BuildAndTestDecisionTree " + 
					"<trainsetFilename> <testsetFilename>\n");
			System.exit(1);
		}    

		// Read in the file names.
		String trainset = args[0];
		String testset  = args[1];

		// Read in the examples from the files.
		ListOfExamples trainExamples = new ListOfExamples();
		ListOfExamples testExamples  = new ListOfExamples();
		if (!trainExamples.ReadInExamplesFromFile(trainset) ||
				!testExamples.ReadInExamplesFromFile(testset))
		{
			System.err.println("Something went wrong reading "
					+ "the datasets ... giving up.");
			System.exit(1);
		}
		else
		{ // The following is included so you can see the data organization.


			//use the TRAINING SET of examples to build a decision tree

			//An array to keep track of features used in the tree
			String[] usedFeatures = 
					new String[trainExamples.getFeatures().length];
			for(int i = 0; i < usedFeatures.length; i++){
				usedFeatures[i] = null;
			}

			//create a root node for the tree using the training set
			Node root = new Node();
			root.setExamples(trainExamples);

			//shuffle the examples
			Collections.shuffle(root.getExamples());
			
			//create a tree using the training set node
			Tree t = new Tree();
			t.buildTree(usedFeatures, root);


			//   prints out the induced decision tree (using simple, indented 
			//   ASCII text)

			t.printTree(root, 0);
			System.out.println();
			System.out.println();


			//The following code (now commented) prints the entire decision
			//tree to a file "output.txt"
			
			/*
			try {
				PrintWriter writer = new PrintWriter("output.txt");
				writer.println("Decision tree printing:");
				writer.println();
				t.printTree(root, 0, writer);
				writer.println();
				writer.println();
				writer.close();

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			 */

			//categorize the TESTING SET using the induced tree, reporting
			// which examples were INCORRECTLY classified, as well as the
			//FRACTION that were incorrectly classified.
			//Only prints out the NAMES of the examples incorrectly classified   

			System.out.println("Names of examples incorrectly classified"
					+ " in the test set:");

			int incorrectCount = 0; //tracks number of incorrect examples

			//run through all examples in test set, checking for correctness
			for(int i = 0; i < testExamples.size(); i++){
				Example ex = testExamples.get(i);
				int result = traverseTree(ex, root);
				if(result == 1){
					System.out.println(ex.getName());
					incorrectCount++;
				}
			}

			System.out.println(); //extra line for readability

			//print fraction of incorrectly classified examples
			System.out.println("Fraction of examples "
					+ "incorrectly classified:");
			System.out.println(incorrectCount + "/" + testExamples.size());
			System.out.println();


			//the following code (commented out) can print out examples from 
			//the training set and the testing set

			//trainExamples.DescribeDataset();
			//testExamples.DescribeDataset();
			//trainExamples.PrintThisExample(0);  // Print out an example
			//trainExamples.PrintAllExamples(); // Don't waste paper printing 
			// all of this out!
			// Instead, just view it on the screen
			//testExamples.PrintAllExamples(); \
		}

		Utilities.waitHere("Hit <enter> when ready to exit.");
	}

	//A method to traverse a tree
	//Returns a 1 if the tree accurately predicts the label
	//of the given example, otherwise returns a 0
	/**
	 *A method to traverse a tree.
	 *Returns a 1 if the tree accurately predicts the label
	 *of the given example, otherwise returns a 0.
	 * 
	 * @param example
	 * @param root
	 * @return 0 if example is correctly classified
	 * 		   1 if example is incorrectly classified
	 */
	public static int traverseTree(Example ex, Node root){

		//Check if node is a leaf
		if(root.getLabel() != null){
			//check if label of leaf matches that of example
			if(ex.getLabel().equalsIgnoreCase(root.getLabel())){
				//if yes, return 0
				return 0;
			}else{
				//if not, return 1
				return 1;
			}
			
		}else{
			
			//cycle through node's children
			for(int i = 0; i < root.getChildren().length; i++){

				//current child
				Node child = root.getChildren()[i];

				//if the child's feature matches the example's
				if(ex.get(child.getFeatureIndex())
						.equalsIgnoreCase(child.getFeature())){
					//call traverseTree on matching child
					return(traverseTree(ex, child));				

				}
			}
		}
		return 0;
	}


}  //end main class


//This class is used to calculate entropy/gain
class Entropy{

	/**
	 *Returns the calculated entropy when given the probability of a positive
	 *example and the probability of a negative example
	 * @param posExamples
	 * @param negExamples
	 * @return entropy
	 */
	public static double infoNeeded(double posExamples, double negExamples){
		double entropy = 0;
		if(posExamples == 0 || negExamples == 0){
			return entropy;
		}else{
			entropy += -posExamples*(Math.log(posExamples)/Math.log(2));
			entropy += -negExamples*(Math.log(negExamples)/Math.log(2));
		}
		return entropy;
	}

	/**
	 *Finds the probability of an example having label 1
	 *in a set of examples
	 * @param examples
	 * @return probability of label 1
	 */
	public static double findPosProbability(ListOfExamples examples){
		BinaryFeature labels = examples.getLabels();
		String label1 = labels.getFirstValue();
		int label1Count = 0;

		for(int i = 0; i < examples.size(); i++){
			//select an example
			Example example = examples.get(i);

			//if the label is positive, add to label count
			if(example.getLabel().equals(label1)){
				label1Count++;
			}
		}
		//return the probability
		return (label1Count/(double)examples.size());
	}

	/**
	 *Finds the probability of an example having label 2
	 *in a set of examples
	 * @param examples
	 * @return probability of label 2
	 */
	public static double findNegProbability(ListOfExamples examples){
		BinaryFeature labels = examples.getLabels();
		String label2 = labels.getSecondValue();
		int label2Count = 0;

		for(int i = 0; i < examples.size(); i++){
			//select an example
			Example example = examples.get(i);

			//if the label is positive, add to label count
			if(example.getLabel().equals(label2)){
				label2Count++;
			}
		}
		//return the probability
		return (label2Count/(double)examples.size());
	}


	/**
	 *This method calculates the gain of a specific feature.  
	 *Requires a list of examples, 
	 *the index of the feature in the list of examples,
	 *and the entropy of the root
	 * @param examples
	 * @param featureIndex
	 * @param rootEntropy
	 * @return gain
	 */
	public static double calculateGain(ListOfExamples examples, 
			int featureIndex, double rootEntropy) {

		double gain = rootEntropy;  //gain

		BinaryFeature labels = examples.getLabels();  //gets the labels 
		//from the set of examples

		String label1 = labels.getFirstValue();       //first label
		String label2 = labels.getSecondValue();      //second label

		//first feature value
		String feature1 = examples.getFeature(featureIndex).getFirstValue();
		//second feature value
		String feature2 = examples.getFeature(featureIndex).getSecondValue();

		//counts examples with first value of given feature
		int feature1Count = 0;  
		//of those with first value, counts how many have label 1
		int f1Label1Count = 0;  
		//of those with first value, counts how many have label 2
		int f1Label2Count = 0;  

		//counts examples with second value of given feature
		int feature2Count = 0;  
		//of those with second value, counts how many have label 1
		int f2Label1Count = 0;  
		//of those with second value, counts how many have label 2
		int f2Label2Count = 0;  

		for(int i = 0; i < examples.size(); i++){

			//select an examples from the list
			Example example = examples.get(i);

			//check feature and label of example and increment accordingly
			if(example.get(featureIndex).equals(feature1)){
				feature1Count++;
				if(example.getLabel().equals(label1)){
					f1Label1Count++;
				}else if(example.getLabel().equals(label2)){
					f1Label2Count++;
				}
			}else if(example.get(featureIndex).equals(feature2)){
				feature2Count++;
				if(example.getLabel().equals(label1)){
					f2Label1Count++;
				}else if(example.getLabel().equals(label2)){
					f2Label2Count++;
				}
			}

		}  //end for loop

		//calculate the gain
		if(feature1Count != 0){
			gain -= (feature1Count/(double)examples.size())*
					(infoNeeded((f1Label1Count/(double)feature1Count), 
							(f1Label2Count/(double)feature1Count)));
		}

		if(feature2Count != 0){
			gain -= (feature2Count/(double)examples.size())*
					(infoNeeded((f2Label1Count/(double)feature2Count),
							(f2Label2Count/(double)feature2Count)));
		}

		//return calculated gain
		return gain;
	}
}


//This class is used to represent a node on our decision tree
class Node {

	private Node parent;                 //parent node
	private Node[] children;             //holds children nodes
	private double entropy;              //entropy of this node
	private ListOfExamples examples;	 //examples in this node
	private BinaryFeature childFeatures; //holds BinaryFeature of children
	private String feature;              //feature that all examples in this
	//node share
	private int featureIndex;            //index of feature of this node
	private String label = null;         //label of this node
	//(applicable if leaf)

	//Constructor
	public Node() {
		this.examples = new ListOfExamples();
		this.entropy = 0.0;
		this.parent = null;
		this.children = null;
		this.childFeatures = new BinaryFeature(null, null, null);
		this.feature = null;
		this.featureIndex = -1;
		this.label = null;
	}

	//sets parent
	public void setParent(Node parent) {
		this.parent = parent;
	}

	//returns parent
	public Node getParent(){
		return parent;
	}

	//sets children
	public void setChildren(Node[] children){
		this.children = children;
	}

	//returns children
	public Node[] getChildren(){
		return children;
	}

	//sets entropy
	public void setEntropy(double entropy){
		this.entropy = entropy;
	}

	//returns entropy
	public void setExamples(ListOfExamples examples){
		this.examples = examples;
	}

	//sets examples
	public ListOfExamples getExamples(){
		return examples;
	}

	//sets label
	public void setLabel(String label){
		this.label = label;
	}

	//sets BinaryFeature for children
	public void setChildFeatures(BinaryFeature childFeatures){
		this.childFeatures = childFeatures;
	}

	//returns BinaryFeature of children
	public BinaryFeature getChildFeatures(){
		return childFeatures;
	}

	//sets feature
	public void setFeature(String feature){
		this.feature = feature;
	}

	//returns feature
	public String getFeature(){
		return feature;
	}

	//sets feature index
	public void setFeatureIndex(int featureIndex){
		this.featureIndex = featureIndex;
	}

	//returns feature index
	public int getFeatureIndex(){
		return featureIndex;
	}

	//returns label
	public String getLabel(){
		return label;
	}

	//returns entropy of node
	public double getEntropy(){
		return entropy;
	}


}

//This class will be used to make up the decision tree
class Tree {

	/**
	 * Builds a decision tree off of the supplied root node using recursion
	 * @param passedUsedFeatures (features used by previous nodes)
	 * @param root
	 * @return root of tree
	 */
	public Node buildTree(String[] passedUsedFeatures, Node root){
		int bestFeatureIndex = -1;        //current best feature index
		BinaryFeature bestFeature = null; //current best feature (most gain)
		double gain = 0;                  //current gain
		double bestGain = -1;             //best gain
		double rootEntropy = 0;           //entropy of given root
		double posExamples;               //probability of label 1 in examples
		double negExamples;               //probability of label 2 in examples
		
		//holds features used up to this node
		String[] usedFeatures = new String[passedUsedFeatures.length];

		//transfer features used previously to usedFeatures
		for(int i = 0; i < passedUsedFeatures.length; i++){
			usedFeatures[i] = passedUsedFeatures[i];
		}


		//probability of getting label 1 and label 2
		posExamples = Entropy.findPosProbability(root.getExamples());
		negExamples = Entropy.findNegProbability(root.getExamples());


		//BASE cases


		//if all examples have the same label, root is leaf with that label
		if(posExamples == 1){
			root.setLabel(root.getExamples().getLabels().getFirstValue());
			return root;
		}else if(negExamples == 1){
			root.setLabel(root.getExamples().getLabels().getSecondValue());
			return root;
		}

		//if there are no features left to check, root is a leaf with majority
		//label of its examples
		int nullCount = 0;
		for(int i = 0; i < usedFeatures.length; i++){
			if(usedFeatures[i] == null){
				nullCount++;
			}
		}
		if(nullCount == 1){
			if(posExamples > negExamples){
				root.setLabel
				(root.getExamples().getLabels().getFirstValue());
			}else if(negExamples > posExamples){
				root.setLabel
				(root.getExamples().getLabels().getSecondValue());
			}else if(negExamples == posExamples){
				root.setLabel
				(root.getExamples().getLabels().getFirstValue());
			}
			return root;
		}

		//if there are no examples, node is a leaf with majority label
		//of parent's examples
		if(root.getExamples().size() == 0){
			Node parent = root.getParent();
			if(parent == null){
				System.out.println("Could not locate parent -- quitting");
				System.exit(0);
			}

			posExamples = Entropy.findPosProbability(parent.getExamples());
			negExamples = Entropy.findNegProbability(parent.getExamples());

			if(posExamples > negExamples){
				root.setLabel
				(parent.getExamples().getLabels().getFirstValue());
			}else if(negExamples > posExamples){
				root.setLabel
				(parent.getExamples().getLabels().getSecondValue());
			}else if(negExamples == posExamples){
				root.setLabel
				(parent.getExamples().getLabels().getFirstValue());
			}	
			return parent;
		}

		//if base cases are not fulfilled, continue

		//find the entropy of the root
		rootEntropy = Entropy.infoNeeded(posExamples, negExamples);

		//set the root entropy
		root.setEntropy(rootEntropy);


		//find the gain for the ith feature
		for(int i = 0; i < (root.getExamples().getNumberOfFeatures()); i++){
			if(usedFeatures[i] == null){
				gain = Entropy.calculateGain(root.getExamples(), 
						i, rootEntropy);

				//if a new best gain is found, replace old best gain
				//and save feature index
				if (gain > bestGain){
					bestGain = gain;
					bestFeatureIndex = i;
				}
			}
		}

		//check for error
		if(bestFeatureIndex == -1){
			System.out.println("Error finding best feature -- quitting");
			System.exit(0);
		}else{
			//set best feature
			bestFeature = 
					root.getExamples().getFeature(bestFeatureIndex);
		}

		//add best feature to list of used features
		usedFeatures[bestFeatureIndex] = bestFeature.getName();
		root.setChildFeatures(bestFeature);

		Node lChild = null; //left child
		Node rChild = null; //right child

		//create subset for left child
		ListOfExamples lChildSubset = subset(root, bestFeature.getFirstValue());
		//if subset is not empty, create left child node
		if(!lChildSubset.isEmpty()){
			lChild = new Node();
			lChild.setParent(root);
			lChild.setExamples(lChildSubset);
			lChild.setFeature(bestFeature.getFirstValue());
			lChild.setFeatureIndex(bestFeatureIndex);
		}

		//create subset for right child
		ListOfExamples rChildSubset = subset(root, bestFeature.getSecondValue());
		//if subset is not empty, create right child node
		if(!rChildSubset.isEmpty()){
			rChild = new Node();
			rChild.setParent(root);
			rChild.setExamples(rChildSubset);
			rChild.setFeature(bestFeature.getSecondValue());
			rChild.setFeatureIndex(bestFeatureIndex);
		}

		//create array for children
		//check if either child is null before creating array
		if(lChild == null){
			Node[] children = {rChild};

			//set children for root
			root.setChildren(children);

			//recursively call on children to continue building tree
			for(int i = 0; i < children.length; i++){
				buildTree(usedFeatures, children[i]);
			}

		}else if(rChild == null){
			Node[] children = {lChild};

			//set children for root
			root.setChildren(children);

			//recursively call on children to continue building tree
			for(int i = 0; i < children.length; i++){
				buildTree(usedFeatures, children[i]);
			}

		}else{
			Node[] children = {lChild, rChild};

			//set children for root
			root.setChildren(children);

			//recursively call on children to continue building tree
			for(int i = 0; i < children.length; i++){
				buildTree(usedFeatures, children[i]);
			}
		}

		//once tree is built, return root
		return root;

	}

	/**
	 *This method, when given a root containing a list of examples,
	 *returns a subset containing all examples with
	 *a feature matching the one given
	 * @param root
	 * @param feature
	 * @return subset containing examples with "feature"
	 */
	public ListOfExamples subset(Node root, String feature){

		ListOfExamples examples = root.getExamples();

		//create subset after parent set
		int numFeatures = examples.getNumberOfFeatures();
		BinaryFeature[] features = examples.getFeatures();
		BinaryFeature outputLabel = examples.getLabels();
		ListOfExamples subset = new ListOfExamples(numFeatures, features,
				outputLabel);

		//add in examples with matching features
		for(int i = 0; i < examples.size(); i++){
			if(examples.get(i).contains(feature)){
				subset.add(examples.get(i));
			}
		}
		
		//sets size of subset
		subset.setNumExamples(subset.size());
		
		//return subset
		return subset;
	}

	/**
	 *This method prints a tree to the standard output,
	 *given the root node of the tree.
	 *Numspace represents the initial indentation of the tree
	 *(typically 0 when called).
	 *
	 * @param root
	 * @param numSpace
	 */
	public void printTree(Node root, int numSpace){

		//BASE case
		//check if node is a leaf
		if(root.getLabel() != null){
			//print label
			System.out.print(" : " + root.getLabel());
			return;
		}

		//for each child
		for(int i = 0; i < root.getChildren().length; i++){
			System.out.println();
			//print indentations
			for(int j = 0; j < numSpace; j++){
				System.out.print("   ");
			}
			//print feature of node
			System.out.print(root.getChildFeatures().getName() + " = ");
			//print feature of ith child
			System.out.print(root.getChildren()[i].getFeature());
			//correct indentation
			numSpace++;
			//call printTree on ith child
			printTree(root.getChildren()[i], numSpace);
			//correct indentation
			numSpace--;
		}
	}

	/**
	 *This method prints a tree to a file "output.txt",
	 *given the root node of the tree and a PrintWriter.
	 *
	 *Numspace represents the initial indentation of the tree
	 *(typically 0 when called).
	 *
	 * @param root
	 * @param numSpace
	 * @param writer
	 */
	public void printTree(Node root, int numSpace, PrintWriter writer){

		//check if node is leaf
		if(root.getLabel() != null){
			//print label
			writer.print(" : " + root.getLabel());
			return;
		}

		//for each child
		for(int i = 0; i < root.getChildren().length; i++){
			writer.println();
			//print indentations
			for(int j = 0; j < numSpace; j++){
				writer.print("   ");
			}
			//print node feature
			writer.print(root.getChildFeatures().getName() + " = ");
			//print ith child feature
			writer.print(root.getChildren()[i].getFeature());
			//correct indentation
			numSpace++;
			//call printTree on ith child
			printTree(root.getChildren()[i], numSpace, writer);
			//correct indentation
			numSpace--;
		}
	}


}

// This class, an extension of ArrayList, holds an individual example.
// The new method PrintFeatures() can be used to
// display the contents of the example. 
// The items in the ArrayList are the feature values.
class Example extends ArrayList<String>
{
	// The name of this example.
	private String name;  

	// The output label of this example.
	private String label;

	// The data set in which this is one example.
	private ListOfExamples parent;  

	// Constructor which stores the dataset which the example belongs to.
	public Example(ListOfExamples parent) {
		this.parent = parent;
	}

	// Print out this example in human-readable form.
	public void PrintFeatures()
	{
		System.out.print("Example " + name + ",  label = " + label + "\n");
		for (int i = 0; i < parent.getNumberOfFeatures(); i++)
		{
			System.out.print("     " + parent.getFeatureName(i)
					+ " = " +  this.get(i) + "\n");
		}
	}

	// Adds a feature value to the example.
	public void addFeatureValue(String value) {
		this.add(value);
	}

	// Accessor methods.
	public String getName() {
		return name;
	}

	public String getLabel() {
		return label;
	}

	// Mutator methods.
	public void setName(String name) {
		this.name = name;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}

/* This class holds all of our examples  from one dataset
   (train OR test, not BOTH).  It extends the ArrayList class.
   Be sure you're not confused.  We're using TWO types of ArrayLists.  
   An Example is an ArrayList of feature values, while a ListOfExamples is 
   an ArrayList of examples. Also, there is one ListOfExamples for the 
   TRAINING SET and one for the TESTING SET. 
 */
class ListOfExamples extends ArrayList<Example>
{
	// The name of the dataset.
	private String nameOfDataset = "";

	// The number of features per example in the dataset.
	private int numFeatures = -1;

	// An array of the parsed features in the data.
	private BinaryFeature[] features;

	// A binary feature representing the output label of the dataset.
	private BinaryFeature outputLabel;

	// The number of examples in the dataset.
	private int numExamples = -1;


	public ListOfExamples() {} 

	//A secondary constructor, used when creating subsets
	public ListOfExamples(int numFeatures, BinaryFeature[] features, BinaryFeature outputLabel){
		this.numFeatures = numFeatures;
		this.features = features;
		this.outputLabel = outputLabel;
		this.numExamples = -1;
	}
	
	//sets Number of Examples in ListOfExamples
	public void setNumExamples(int numExamples){
		this.numExamples = numExamples;
	}

	// Print out a high-level description of the dataset including its features.
	public void DescribeDataset()
	{
		System.out.println("Dataset '" + nameOfDataset + "' contains "
				+ numExamples + " examples, each with "
				+ numFeatures + " features.");
		System.out.println("Valid category labels: "
				+ outputLabel.getFirstValue() + ", "
				+ outputLabel.getSecondValue());
		System.out.println("The feature names (with their possible values) are:");
		for (int i = 0; i < numFeatures; i++)
		{
			BinaryFeature f = features[i];
			System.out.println("   " + f.getName() + " (" + f.getFirstValue() +
					" or " + f.getSecondValue() + ")");
		}
		System.out.println();
	}

	// Print out ALL the examples.
	public void PrintAllExamples()
	{
		System.out.println("List of Examples\n================");
		for (int i = 0; i < size(); i++)
		{
			Example thisExample = this.get(i);  
			thisExample.PrintFeatures();
		}
	}

	// Print out the SPECIFIED example.
	public void PrintThisExample(int i)
	{
		Example thisExample = this.get(i); 
		thisExample.PrintFeatures();
	}

	// Returns the number of examples in the data
	public int getNumberofExamples() {
		return numExamples;
	}

	// Returns the number of features in the data.
	public int getNumberOfFeatures() {
		return numFeatures;
	}

	//Returns an array of features in the data
	public BinaryFeature[] getFeatures(){
		return features;
	}

	// Returns the ith BinaryFeature
	public BinaryFeature getFeature(int i) {
		return features[i];
	}

	// Returns the name of the ith feature.
	public String getFeatureName(int i) {
		return features[i].getName();
	}

	//Returns the BinaryFeature containing the output label
	public BinaryFeature getLabels(){
		return outputLabel;
	}

	// Takes the name of an input file and attempts to open it for parsing.
	// If it is successful, it reads the dataset into its internal structures.
	// Returns true if the read was successful.
	public boolean ReadInExamplesFromFile(String dataFile) {
		nameOfDataset = dataFile;

		// Try creating a scanner to read the input file.
		Scanner fileScanner = null;
		try {
			fileScanner = new Scanner(new File(dataFile));
		} catch(FileNotFoundException e) {
			return false;
		}

		// If the file was successfully opened, read the file
		this.parse(fileScanner);
		return true;
	}

	/**
	 * Does the actual parsing work. We assume that the file is in proper format.
	 *
	 * @param fileScanner a Scanner which has been successfully opened to read
	 * the dataset file
	 */
	public void parse(Scanner fileScanner) {
		// Read the number of features per example.
		numFeatures = Integer.parseInt(parseSingleToken(fileScanner));

		// Parse the features from the file.
		parseFeatures(fileScanner);

		// Read the two possible output label values.
		String labelName = "output";
		String firstValue = parseSingleToken(fileScanner);
		String secondValue = parseSingleToken(fileScanner);
		outputLabel = new BinaryFeature(labelName, firstValue, secondValue);

		// Read the number of examples from the file.
		numExamples = Integer.parseInt(parseSingleToken(fileScanner));

		parseExamples(fileScanner);
	}

	/**
	 * Returns the first token encountered on a significant line in the file.
	 *
	 * @param fileScanner a Scanner used to read the file.
	 */
	private String parseSingleToken(Scanner fileScanner) {
		String line = findSignificantLine(fileScanner);

		// Once we find a significant line, parse the first token on the
		// line and return it.
		Scanner lineScanner = new Scanner(line);
		return lineScanner.next();
	}

	/**
	 * Reads in the feature metadata from the file.
	 * 
	 * @param fileScanner a Scanner used to read the file.
	 */
	private void parseFeatures(Scanner fileScanner) {
		// Initialize the array of features to fill.
		features = new BinaryFeature[numFeatures];

		for(int i = 0; i < numFeatures; i++) {
			String line = findSignificantLine(fileScanner);

			// Once we find a significant line, read the feature description
			// from it.
			Scanner lineScanner = new Scanner(line);
			String name = lineScanner.next();
			String dash = lineScanner.next();  // Skip the dash in the file.
			String firstValue = lineScanner.next();
			String secondValue = lineScanner.next();
			features[i] = new BinaryFeature(name, firstValue, secondValue);
		}
	}

	private void parseExamples(Scanner fileScanner) {
		// Parse the expected number of examples.
		for(int i = 0; i < numExamples; i++) {
			String line = findSignificantLine(fileScanner);
			Scanner lineScanner = new Scanner(line);

			// Parse a new example from the file.
			Example ex = new Example(this);

			String name = lineScanner.next();
			ex.setName(name);

			String label = lineScanner.next();
			ex.setLabel(label);

			// Iterate through the features and increment the count for any feature
			// that has the first possible value.
			for(int j = 0; j < numFeatures; j++) {
				String feature = lineScanner.next();
				ex.addFeatureValue(feature);
			}

			// Add this example to the list.
			this.add(ex);
		}
	}

	/**
	 * Returns the next line in the file which is significant (i.e. is not
	 * all whitespace or a comment.
	 *
	 * @param fileScanner a Scanner used to read the file
	 */
	private String findSignificantLine(Scanner fileScanner) {
		// Keep scanning lines until we find a significant one.
		while(fileScanner.hasNextLine()) {
			String line = fileScanner.nextLine().trim();
			if (isLineSignificant(line)) {
				return line;
			}
		}

		// If the file is in proper format, this should never happen.
		System.err.println("Unexpected problem in findSignificantLine.");

		return null;
	}

	/**
	 * Returns whether the given line is significant (i.e., not blank or a
	 * comment). The line should be trimmed before calling this.
	 *
	 * @param line the line to check
	 */
	private boolean isLineSignificant(String line) {
		// Blank lines are not significant.
		if(line.length() == 0) {
			return false;
		}

		// Lines which have consecutive forward slashes as their first two
		// characters are comments and are not significant.
		if(line.length() > 2 && line.substring(0,2).equals("//")) {
			return false;
		}

		return true;
	}
}

/**
 * Represents a single binary feature with two String values.
 */
class BinaryFeature {
	private String name;
	private String firstValue;
	private String secondValue;

	public BinaryFeature(String name, String first, String second) {
		this.name = name;
		firstValue = first;
		secondValue = second;
	}

	public String getName() {
		return name;
	}

	public String getFirstValue() {
		return firstValue;
	}

	public String getSecondValue() {
		return secondValue;
	}
}

class Utilities
{
	// This method can be used to wait until you're ready to proceed.
	public static void waitHere(String msg)
	{
		System.out.print("\n" + msg);
		try { System.in.read(); }
		catch(Exception e) {} // Ignore any errors while reading.
	}
}
