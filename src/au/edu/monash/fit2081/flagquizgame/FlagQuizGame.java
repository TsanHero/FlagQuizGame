// FlagQuizGame.java
// Main Activity for the Flag Quiz Game App
package au.edu.monash.fit2081.flagquizgame;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.lang.String;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

public class FlagQuizGame extends Activity{
   // String used when logging error messages
   private static final String TAG = "FlagQuizGame Activity"; //logCat filter
   
   private List<String> fileNameList; 			// fnames of all flags in enabled regions
   private List<String> quizCountriesList; 		// fnames of all 10 flags to be displayed
   private Map<String, Boolean> regionsMap; 	// which regions are enabled
   private String correctAnswer; 				// fname of the current flag displayed
   private int totalGuesses; 					// number of guesses made
   private int correctAnswers; 					// number of correct guesses
   private int guessRows; 						// number of rows displaying choices
   private Random random; 						// random number generator
   private Handler handler; 					// used to delay loading next flag
   private Animation shakeAnimation; 			// animation for incorrect guess
   private Animation shakeAnimation2;
   private String currentregion;
   private int nothing = 0;
   private String[][] regions = new String[3][3];;
   private TextView answerTextView; 			// displays Correct! or Incorrect!
   private TextView questionNumberTextView; 	// shows current question #
   private ImageView flagImageView; 			// displays a flag
   private TableLayout buttonTableLayout; 		// table of answer Buttons
   
   // called when the activity is first created
   @Override
   public void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState); // call the superclass's method
      setContentView(R.layout.main); // inflate the main GUI

      //initialise instance variables
      fileNameList = new ArrayList<String>();
      quizCountriesList = new ArrayList<String>(); 	// flags in this quiz
      regionsMap = new HashMap<String, Boolean>(); 	// HashMap of regions
      guessRows = 1; 								// default to one row of choices, user can customise
      random = new Random(); 						// construct the random number generator
      handler = new Handler(); 						// used to perform delayed operations

      // load the shake animation that's used for incorrect answers
      shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.incorrect_shake); 
      shakeAnimation.setRepeatCount(3);
      shakeAnimation2 = AnimationUtils.loadAnimation(this, R.anim.same_region_shake);
      shakeAnimation2.setRepeatCount(3);// animation repeats 3 times
      
      // get array of world regions from strings.xml
      String[] regionNames = getResources().getStringArray(R.array.regionsList);
      
      // by default, countries are chosen from all regions, user can customise
      for (String region : regionNames )
         regionsMap.put(region, true);

      // get references to GUI components (initialise instance variables)
      questionNumberTextView = (TextView) findViewById(R.id.questionNumberTextView);
      flagImageView = (ImageView) findViewById(R.id.flagImageView);
      buttonTableLayout = (TableLayout) findViewById(R.id.buttonTableLayout);
      answerTextView = (TextView) findViewById(R.id.answerTextView);

      // set questionNumberTextView's text
//      questionNumberTextView.setText(
//         getResources().getString(R.string.question) + " 1 " + 
//         getResources().getString(R.string.of) + " 10");
      
      //questionNumberTextView.setText(
    	         //R.string.question + " 1 " + R.string.of + " 10");      

      resetQuiz(); // start a new quiz
   } 

   // set up and start the next quiz 
   private void resetQuiz() {      
      // use the AssetManager to get the flag image filenames for enabled regions
      AssetManager assets = getAssets(); 	// get the App's AssetManager with an Activity method
      fileNameList.clear(); 				// empty the list of all filenames
      
      try{
         Set<String> regions = regionsMap.keySet(); // get Set of regions

         // load fileNameList with the flag image file names (minus extension)
         // of all eligible countries (i.e. they are in an eligible region)
         for (String region : regions){
            if (regionsMap.get(region)){ // if region is enabled, get(key) returns key's value
               // get a list of all flag image file names in this region
               String[] paths = assets.list(region); //WOW!!!, region used as a folder name

               for (String path : paths) //get rid of file extension 
                  fileNameList.add(path.replace(".png", ""));
            }
         }
      }
      catch (IOException e){
         Log.e(TAG, "Error loading image file names", e);
      }
      
      correctAnswers = 0;
      totalGuesses = 0;
      quizCountriesList.clear(); // init list of candidate countries in this quiz
      
      // add 10 random file names to the quizCountriesList
      int flagCounter = 1; 
      int numberOfFlags = fileNameList.size(); // get number of available flags

      while (flagCounter <= 10) {
         int randomIndex = random.nextInt(numberOfFlags); // random index

         // get the random country name
         String fileName = fileNameList.get(randomIndex);
         
         // if the file name hasn't already been chosen add it to quizCountriesList
         if (!quizCountriesList.contains(fileName)){
            quizCountriesList.add(fileName); // add the file name to the list
            ++flagCounter;
         }
         
         Log.d("correct", fileName);
      }

      loadNextFlag(); // start the quiz by loading the first flag
   } 

   // after the user guesses a correct flag or at the start of a new quiz, load the next flag
   // we don't come here after an incorrect guess in this case the question stays the same
   private void loadNextFlag(){
	  // like removing plates from the bottom of a stack of plates 
	  // remove it from list so it doesn't get picked again
	  // get next one from whatever is left of the 10 correct answers for this quiz

      String nextImageName = quizCountriesList.remove(0);
      correctAnswer = nextImageName; // update the correct answer

      answerTextView.setText(""); // clear answerTextView (incorrect: "incorrect" in red, correct: country name in green)

      // remember a quiz has 10 questions - each question eventually ends with a correct answer 
      questionNumberTextView.setText(	getResources().getString(R.string.question) + " " + (correctAnswers + 1) + " " + 
    		  							getResources().getString(R.string.of) + " 10");

      // extract the region name from the correct answer (format e.g. Europe-United_Kingdom)
      // this is the name of the folder where the flag image file of the correct answer is kept
      String region = nextImageName.substring(0, nextImageName.indexOf('-')); //from index 0 up to one less than index of -
      currentregion=region;
      // use AssetManager to load next image from assets folder
      AssetManager assets = getAssets(); 		// get app's AssetManager using Activity's getAssets() method
      InputStream stream; 						// used to read in flag images

      try{ // anything can happen in the external world
         // get an InputStream to image of the flag to display for this question
         stream = assets.open(region + "/" + nextImageName + ".png");
         
         // load the asset as a Drawable and display on the flagImageView
         Drawable flag = Drawable.createFromStream(stream, nextImageName); //2nd param is nonsense not currently used
         flagImageView.setImageDrawable(flag); //put image into ImageView                       
      }
      catch (IOException e){ 
         Log.e(TAG, "Error loading " + nextImageName, e);
      }

      // clear guess buttons row by row (not cell by cell)
      // NOTE: buttonTableLayout tableRows are created by main layout and never deleted 
      // just filled, emptied and refilled with child Views (specifically guess buttons) 
      // getChildAt() returns a View so down casting is required  
      for (int row = 0; row < buttonTableLayout.getChildCount(); ++row)
         ((TableRow) buttonTableLayout.getChildAt(row)).removeAllViews();

      // shuffle list of file names from enabled regions using static method of Collections
      // ready to assign the first 3, 6, 9 for new guess buttons
      Collections.shuffle(fileNameList); 

      // put the correct answer at the end of the list of file names from enabled regions (i.e. move it out of the first 3, 6, 9). Unsafe!!!
      // The first 3, 6, 9 of the list of file names from enabled regions are assigned in the nested for loops below
      // Then one of these guess button is reassigned the correct answer
      // We don't want two correct guess buttons
      // what if eligible number of countries is < 3, 6, 9 e.g. all regions are disabled
      int correct = fileNameList.indexOf(correctAnswer);
      fileNameList.add(fileNameList.remove(correct)); //remove the correct file name and also returns it so can add it at end

      // get a reference to the LayoutInflater service
      LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

      // add 3, 6, or 9 answer Buttons based on the value of guessRows
      for (int row = 0; row < guessRows; row++) {
         TableRow currentTableRow = getTableRow(row); //private helper method call

         // place Buttons in currentTableRow
         for (int column = 0; column < 3; column++){
            // inflate guess_button.xml to create new Button, null = no parent to attach to nominated yet (see below)
            Button newGuessButton = (Button) inflater.inflate(R.layout.guess_button, null);
      
            // get file name from first 3, 6, 9 of list, format it for button text and set it as newGuessButton's text
            // row 0: index is 0,1,2   row 1: index is 3,4,5   row 2: index is 6,7,8 
            String fileName = fileNameList.get((row * 3) + column);
            newGuessButton.setText(getCountryName(fileName)); //private method call
            regions[row][column]=fileName;
            
            // register listener object guessButtonListener to respond to button clicks
            // need to code below
            // Note: all buttons have the same listener
            newGuessButton.setOnClickListener(guessButtonListener);
            
            // add button to current row
            currentTableRow.addView(newGuessButton); // note: table row orientation set to horizontal in layout
         } //finish this column 
      } //finish this row
      
      // guess buttons all created
      
      // randomly replace one Button with the correct answer
      int row = random.nextInt(guessRows); 	// pick random row
      int column = random.nextInt(3); 		// pick random column
      
      TableRow randomTableRow = getTableRow(row); // get the TableRow
      
      String countryName = getCountryName(correctAnswer); //get presentable version of the country name
      ((Button)randomTableRow.getChildAt(column)).setText(countryName);    
   }

   // returns the specified TableRow
   private TableRow getTableRow(int row){
      return (TableRow) buttonTableLayout.getChildAt(row);
   }
   
   // parses the country flag file name and returns the country name
   private String getRegion(String name){
      return name.substring(0, name.indexOf('-'));
   }
   private String getCountryName(String name){
	  // start after hyphen (after region-) then change underscores for spaces  
      return name.substring(name.indexOf('-') + 1).replace('_', ' ');
   }
   
   // called when the user selects an answer by clicking a guess button
   private void submitGuess(Button guessButton){
	  //String guess = guessButton.getText().toString();
      String guess = (String) guessButton.getText(); //is this equivalent to the statement above?
      boolean sameRegion=false;
      String answer = getCountryName(correctAnswer); //get presentable version of the country name

      ++totalGuesses; // increment the number of guesses the user has made
      for(int i = 0; i<guessRows ;i++) {
         for(int j=0;j<3;j++ ) {
            if (getCountryName(regions[i][j]).equals(guess)) {
               if (getRegion(regions[i][j]).equals(currentregion)) {
                  sameRegion = true;
               }
            }
         }

      }

      // if the guess is correct
      if (guess.equals(answer)||sameRegion==true){
         ++correctAnswers; // increment the number of correct answers

         // display "Correct!" in green text
         if (guess.equals(answer)){
            answerTextView.setText(answer + "!");
            answerTextView.setTextColor(getResources().getColor(R.color.correct_answer));
         }
         else{
            answerTextView.setText("close enough!");
            answerTextView.setTextColor(getResources().getColor(R.color.same_region));
            flagImageView.startAnimation(shakeAnimation2);
         }
         disableButtons(); // disable all answer Buttons
         
         // if the user has correctly identified 10 flags then the Quiz Over
         if (correctAnswers == 10){
            // create a new AlertDialog Builder
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(R.string.reset_quiz); // title bar string
            
            // set the AlertDialog's message to display game results
            // <<<<<
            builder.setMessage(String.format("%d %s, %.02f%% %s", 
               totalGuesses, getResources().getString(R.string.guesses), 
               (1000 / (double) totalGuesses), getResources().getString(R.string.correct)));

            builder.setCancelable(false); // back key will not cancel dialog so must press reset to dismiss 
            
            // add "Reset Quiz" Button                              
            builder.setPositiveButton(R.string.reset_quiz, new DialogInterface.OnClickListener()                
               { //anonymous inner class + dialog button listener instantiation                                                       
                  public void onClick(DialogInterface dialog, int id){
                     resetQuiz();                                      
                  }                             
               }
            ); //end setPositiveButton call - param1: button text, param2: anonymous listener object instantiated in situ using an anonymous inner class syntax
            
            // build and show
            AlertDialog resetDialog = builder.create();
            resetDialog.show();
         }
         else{ // last guess correct but correctAnswers < 10
            // load the next flag after a 1-second delay
            handler.postDelayed(
               new Runnable() { 
                  @Override
                  public void run(){
                     loadNextFlag();
                  }
               }, 1000); // end of postDelayed method - param1: in situ instantiatin of an anonymous Runnable, param2: 1000 milliseconds for 1-second delay
         }
      }
      else{ // guess was incorrect  
         // play the animation
         flagImageView.startAnimation(shakeAnimation);

         // display "Incorrect!" in red 
         answerTextView.setText(R.string.incorrect_answer);
         answerTextView.setTextColor(getResources().getColor(R.color.incorrect_answer));
         
         guessButton.setEnabled(false); // disable the incorrect guess button
         
         //nothing else to do: user clicked a incorrect guess button, we are still in the same quiz, the only difference is one guess button has been disabled
      } 
   }

   // utility method that disables all answer Buttons 
   private void disableButtons(){
      for (int row = 0; row < buttonTableLayout.getChildCount(); ++row){
         TableRow tableRow = (TableRow) buttonTableLayout.getChildAt(row);
         for (int i = 0; i < tableRow.getChildCount(); ++i){
            tableRow.getChildAt(i).setEnabled(false);
         }
      }
   }
   
   
   
   // class level declarations
   // create constants to ID each menu item
   private final int CHOICES_MENU_ID = Menu.FIRST;
   private final int REGIONS_MENU_ID = Menu.FIRST + 1;

   
   // called when the user accesses the options menu
   @Override
   public boolean onCreateOptionsMenu(Menu menu){            
      super.onCreateOptionsMenu(menu);                        
                                                              
      // add two options to the menu - "Choices" and "Regions"
      // params: group ID (don't care), menu option ID, menu option order (don't care), resource ID for menu option's text 
      menu.add(Menu.NONE, CHOICES_MENU_ID, Menu.NONE, R.string.choices);             
      menu.add(Menu.NONE, REGIONS_MENU_ID, Menu.NONE, R.string.regions);             
                                                              
      return true; // must do this to display the menu                        
   }                       

   // called when the user selects menu item from the menu
   @Override
   public boolean onOptionsItemSelected(MenuItem item){
      // switch the menu id of the user-selected option
      switch (item.getItemId()) {
         case CHOICES_MENU_ID:
            // create a list of the possible numbers of answer choices
            final String[] possibleChoices = getResources().getStringArray(R.array.guessesList); // final required !!!!!

            // create a new AlertDialog Builder and set its title
            AlertDialog.Builder choicesBuilder = new AlertDialog.Builder(this);
            
            choicesBuilder.setTitle(R.string.choices);
         
            // add possibleChoices's items to the Dialog and set the behaviour when one of the items is clicked
            // setItems(...) is new we usually just setMessage(...)
            // <<<<<
            choicesBuilder.setItems(R.array.guessesList,                    
               new DialogInterface.OnClickListener()                    
               {                                                        
                  public void onClick(DialogInterface dialog, int item){
                	 // this executes after any item click then Android closes dialog immediately
                     // update guessRows to match the user's choice     
                     guessRows = Integer.parseInt(possibleChoices[item].toString()) / 3;          
                     resetQuiz();                    
                  }                              
               }
            ); // this is the end of the call to setItems(...)                             
         
            // create an AlertDialog from the Builder
            AlertDialog choicesDialog = choicesBuilder.create();
            choicesDialog.show(); // show the Dialog            
            return true; 

         case REGIONS_MENU_ID:
            // get array of regions (enabled or disabled) to display as list in dialog box
            final String[] regionNames = regionsMap.keySet().toArray(new String[regionsMap.size()]); // final required !!!!!

            // get PARALLEL enabled/disabled array of booleans to display current check box state for each region
            final boolean[] regionsEnabled = new boolean[regionsMap.size()];
            for (int i = 0; i < regionsEnabled.length; ++i)
               regionsEnabled[i] = regionsMap.get(regionNames[i]);


            // create an AlertDialog Builder and set the dialog's title
            final AlertDialog.Builder regionsBuilder = new AlertDialog.Builder(this);
            regionsBuilder.setTitle(R.string.regions);



            // replace _ with space in region names for display purposes
            String[] displayNames = new String[regionNames.length];
            for (int i = 0; i < regionNames.length; ++i)
               displayNames[i] = regionNames[i].replace('_', ' ');
         
            // add displayNames to the Dialog and set the behaviour when one of the items is clicked
            // setMultiChoiceItems(...) is new we usually just setMessage(...)
            // <<<<<

            regionsBuilder.setMultiChoiceItems(
               displayNames, regionsEnabled,
               new DialogInterface.OnMultiChoiceClickListener() 
               {

                  @Override
                  public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                     // this executes on every selection/deselection - dialog does not close - +ve button handles this
                     // include or exclude the clicked region depending on whether or not it's checked



                     if (isChecked == false) {
                        nothing = nothing + 1;
                     }
                     else {
                        nothing = nothing - 1;
                        ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                     }
                     if (nothing == 6) {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "You clears all,Please select at least one region. Thank you!", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
                        toast.show();
                        ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                     }
                     else {
                        regionsMap.put(regionNames[which].toString(), isChecked); //overwrites existing, will have an effect when resetQiz() is called see below
                     }
                  }
               } // end anonymous inner class
            ); // end call to setMultiChoiceItems
          
            // resets quiz when user presses the "Reset Quiz" Button
            regionsBuilder.setPositiveButton(R.string.reset_quiz,
               new DialogInterface.OnClickListener(){
                  @Override
                  public void onClick(DialogInterface dialog, int button)
                  {
                     resetQuiz();
                  }
               } // end anonymous inner class
            ); // end call to method setPositiveButton
                        
            // create and show 
            AlertDialog regionsDialog = regionsBuilder.create();
            regionsDialog.show();
            
            return true;
      } // end switch

      return super.onOptionsItemSelected(item);
   }

   // called when a guess Button is clicked
   private OnClickListener guessButtonListener = new OnClickListener(){
      @Override
      public void onClick(View v) {
         submitGuess((Button) v); // pass selected Button to submitGuess
      }
   }; // end guessButtonListener declaration
   
} // end FlagQuizGame
     
/*************************************************************************
* (C) Copyright 1992-2012 by Deitel & Associates, Inc. and               *
* Pearson Education, Inc. All Rights Reserved.                           *
*                                                                        *
* DISCLAIMER: The authors and publisher of this book have used their     *
* best efforts in preparing the book. These efforts include the          *
* development, research, and testing of the theories and programs        *
* to determine their effectiveness. The authors and publisher make       *
* no warranty of any kind, expressed or implied, with regard to these    *
* programs or to the documentation contained in these books. The authors *
* and publisher shall not be liable in any event for incidental or       *
* consequential damages in connection with, or arising out of, the       *
* furnishing, performance, or use of these programs.                     *
*************************************************************************/
