package net.lukecameron.marvelcharacterviewer;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity {


    private TextView m_DummyView;
    private LinearLayout m_LinearLayout;
    private TextView m_MarvelAttributionLink;

    private EditText m_IndexText;
    private TextView m_TotalText;

    private TextView m_Title;
    private TextView m_Description;
    private TextView m_Link;

    private ImageView m_Image;

    private int m_CharIndex = 0;

    GetMarvelCharacters m_MarvelCharacters;
    Boolean m_SomeCharactersDownloaded = false; //starts false, will be updated by m_MarvelCharacters when a character is downloaded


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get the existing views
        GetViews();
        //create the new ones
        InitViews();

        //create an instance of GetMarvelCharacters and set it running
        m_MarvelCharacters = new GetMarvelCharacters(this);
        m_MarvelCharacters.execute();

        //begin checking for the first character to be downloaded
        CheckForCharsRecursively();
    }

    //functions for our buttons to use
    public void OnPreviousClick(View view){
        ChangeCharacter(-1);
    }

    public void OnNextClick(View view){
        ChangeCharacter(1);
    }

    public void Go(View view){
        //prevent errors from people entering non-integer strings we aren't handling
        try{
            String input = m_IndexText.getText().toString();
            if(input.equals("List") || input.equals("0")){
                m_CharIndex = -1;
                DisplayListOfCharacters();
            }
            else {
                //get the number entered
                int newIndex = Integer.parseInt(m_IndexText.getText().toString());
                //check that it's valid, minus 1 so that from the user's perspective the index starts at 1
                if(IsValidIndex(newIndex - 1)){
                    m_CharIndex = newIndex - 1;
                    UpdateViews();
                }
            }
        }
        catch (Exception e){
        }

    }


    public void GetViews(){
        m_DummyView = (TextView)findViewById(R.id.testText);
        m_DummyView.setText("");

        //set up the marvel attribution link
        m_MarvelAttributionLink = (TextView)findViewById(R.id.marvelAttribution);
        m_MarvelAttributionLink.setText("Data provided by Marvel. © 2018 Marvel");
        m_MarvelAttributionLink.setTextColor(Color.BLUE);
        //add the on click functionality
        m_MarvelAttributionLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Uri address= Uri.parse("http://marvel.com");
                Intent intent= new Intent(Intent.ACTION_VIEW, address);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        m_IndexText = (EditText)findViewById(R.id.editText);
        m_TotalText = (TextView)findViewById(R.id.totalText);

        m_LinearLayout = (LinearLayout)findViewById(R.id.linearLayout);
    }


    //initialise the runtime added views
    public void InitViews(){
        m_Title = MakeNewText("Loading", Typeface.BOLD, m_DummyView);
        m_Description = MakeNewText("", Typeface.NORMAL, m_Title);
        m_Link = MakeNewText("", Typeface.BOLD, m_Description, "http://marvel.com");
    }



    //runs recursively until the first character is downloaded
    public void CheckForCharsRecursively(){
        //wait for 0.1 seconds
        new CountDownTimer(100, 100){
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                //if there is a character, zero out the index and update the views
                if(m_SomeCharactersDownloaded){
                    m_CharIndex = -1;
                    DisplayListOfCharacters();
                //if not, start this process again
                }else{
                    CheckForCharsRecursively();
                }
            }
        }.start();
    }




    void DisplayListOfCharacters(){
        //initialise an empty string
        String charList = "";
        //copy the list of characters from m_MarvelCharacters
        List<GetMarvelCharacters.Character> tempList = new ArrayList<>(m_MarvelCharacters.getCharacterList());
        //iterate through
        for(int i = 0; i < tempList.size(); i++){
            //Append index and name to our string
            charList += Integer.toString(i + 1) + ". " + tempList.get(i).CharName + "\n";
        }
        //set the title text view to our new string and clear everything else
        m_Title.setText(charList);
        m_Description.setText("");
        m_Link.setText("");
        if(m_Image != null) m_LinearLayout.removeView(m_Image);

        //update the text that displays the total characters that have been downloaded
        UpdateTotal();

        //Set the input field's text to 'List'
        m_IndexText.setText("List");
    }





    void UpdateViews(){
        //get the content strings
        String title = m_MarvelCharacters.getCharacterList().get(m_CharIndex).CharName;
        String descr = m_MarvelCharacters.getCharacterList().get(m_CharIndex).CharDescr;
        //if there is a description, add new lines after the title and description
        if(descr != null && !descr.isEmpty()) {
            title += "\n";
            descr += "\n";
        }
        //set the Text Views
        m_Title.setText(title);
        m_Description.setText(descr);
        //update where the 'find out more' link goes
        UpdateText(m_Link, "Find Out More", m_MarvelCharacters.getCharacterList().get(m_CharIndex).CharInfoURL);
        //load the new image
        MakeNewImage(m_MarvelCharacters.getCharacterList().get(m_CharIndex).CharImageURL);
        //update the text that displays the total characters that have been downloaded
        UpdateTotal();
    }


    void UpdateTotal(){
        //format the string
        String total = "/" + Integer.toString(m_MarvelCharacters.NumberOfDownloadedChars());
        //and set it
        m_TotalText.setText(total);
    }


    //Edited from https://android--code.blogspot.com/2015/08/android-imageview-programmatically.html
    void MakeNewImage(String url){
        //check if an image already exists and delete it if it does
        if(m_Image != null) m_LinearLayout.removeView(m_Image);

        // Initialize a new ImageView widget
        ImageView iv = new ImageView(getApplicationContext());

        Picasso.get().load(url).into(iv);

        // Create layout parameters for ImageView
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        // Add the ImageView below the last text view
        lp.addRule(RelativeLayout.BELOW, m_Link.getId());

        // Add layout parameters to ImageView
        iv.setLayoutParams(lp);

        // Finally, add the ImageView to layout
        m_LinearLayout.addView(iv);

        //store the image view so that we can remove it later
        m_Image = iv;
    }





    void UpdateText(TextView text, String content, final String link){
        //set the text string
        text.setText(content);

        //add the on click functionality
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Uri address= Uri.parse(link);
                Intent intent= new Intent(Intent.ACTION_VIEW, address);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }



    TextView MakeNewText(String content, int typefaceStyle, View viewAbove){
        // Initialize a new ImageView widget
        TextView tv = new TextView(getApplicationContext());

        tv.setText(content);
        tv.setTextColor(Color.BLACK);

        tv.setTypeface(null, typefaceStyle);

        // Create layout parameters for ImageView
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        // Add rule to layout parameters
        // Add the ImageView below to Button
        lp.addRule(RelativeLayout.BELOW, viewAbove.getId());

        // Add layout parameters to ImageView
        tv.setLayoutParams(lp);

        // Finally, add the ImageView to layout
        m_LinearLayout.addView(tv);

        return tv;
    }



    TextView MakeNewText(String content, int typefaceStyle, View viewAbove, final String link){
        // Initialize a new ImageView widget
        TextView tv = new TextView(getApplicationContext());

        tv.setText(content);
        tv.setTextColor(Color.BLUE);

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Uri address= Uri.parse(link);
                Intent intent= new Intent(Intent.ACTION_VIEW, address);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        //set the typeface style (normal/bold/etc)
        tv.setTypeface(null, typefaceStyle);

        // Create layout parameters for ImageView
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        // Add rule to layout parameters
        // Add the ImageView below to Button
        lp.addRule(RelativeLayout.BELOW, viewAbove.getId());

        // Add layout parameters to ImageView
        tv.setLayoutParams(lp);

        // Finally, add the ImageView to layout
        m_LinearLayout.addView(tv);

        return tv;
    }

    //change to an adjacent character
    //intended to take arguments of 1 or -1
    void ChangeCharacter(int dif){
        //if the user is trying to go back from the first entry, display the list again
        if(m_CharIndex + dif == -1){
            DisplayListOfCharacters();
            m_CharIndex = -1;
        }
        else {
            //only actually do anything if there are character to change to
            if(m_SomeCharactersDownloaded){
                //check the index is valid
                if(IsValidIndex(m_CharIndex + dif)){
                    //apply the change to the index
                    m_CharIndex += dif;
                    //update the views
                    UpdateViews();
                    //update the number displayed in the input field, plus one so that from the user's perspective the index starts at 1
                    m_IndexText.setText(Integer.toString(m_CharIndex + 1));
                }
            }
        }
    }


    //checks that the input is between 0 and last index of the downloaded character array (both inclusive)
    private Boolean IsValidIndex(int testIndex){
        return testIndex >= 0 && testIndex <= m_MarvelCharacters.NumberOfDownloadedChars()-1;
    }


    //for the AsyncTask to tell main that it has finished downloading a character
    public void SomeCharactersLoaded(){
        if(!m_SomeCharactersDownloaded) {
            m_SomeCharactersDownloaded = true;
        }
    }
}
