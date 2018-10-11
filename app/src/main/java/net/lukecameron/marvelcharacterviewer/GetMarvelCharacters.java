package net.lukecameron.marvelcharacterviewer;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
//import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.*;
import static java.sql.DriverManager.println;

public class GetMarvelCharacters extends AsyncTask {

    public List<Character> m_CharacterList = new ArrayList<>();

    private Boolean m_AllCharsDownloaded = false;

    public Boolean getAllCharsDownloaded() {
        return m_AllCharsDownloaded;
    }

    String m_RawDataString;
    JsonObject m_CharactersJSON;
    JsonArray m_CharacterArray = null;

    //this will be set to a higher value after the first lot of character is downloaded
    int m_TotalCharacters = 1;
    public int getTotalCharacters(){
        return m_TotalCharacters;
    }

    //url will always start with this
    String base = "http://gateway.marvel.com/v1/public/characters?";
    //url will always end with this
    String apikey = "&ts=1&apikey=846dae85275e8b6750d60e755ac65351&hash=9b6b530fefb1b697e784403972168a61";

    private MainActivity mainActivity;


    public GetMarvelCharacters(MainActivity main){
        this.mainActivity = main;
    }


    public void GetMarvelData() throws IOException {

        for(int i = 0; i < m_TotalCharacters; i += 100){

            //format the text in between our set url string
            String param = "limit=100&offset=" + Integer.toString(i);

            //concat the url strings and get a URL
            String fullURL = base + param + apikey;
            URL actualURL = new URL(fullURL);

            //open the connection and set the request method
            HttpURLConnection connection = (HttpURLConnection) actualURL.openConnection();
            connection.setRequestMethod("GET");

            //like, just, don't time out
            connection.setConnectTimeout(50000);
            connection.setReadTimeout(50000);

            //create a buffer reader to read from our connection
            BufferedReader in = new BufferedReader( new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            //while there is still stuff coming out of the pipes, add it to the string buffer
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            //and when there isn't, close the buffer reader
            in.close();

            //convert the stringbuffer to a string
            m_RawDataString = content.toString();

            //convert that string to a JSON Object
            m_CharactersJSON = (new JsonParser()).parse(m_RawDataString).getAsJsonObject();

            //get the 'data' object
            JsonObject data = m_CharactersJSON.getAsJsonObject("data");
            //and from that, get the total characters that Marvel will be sending us
            String totalCharacterString = data.get("total").toString();

            //parse and store as an integer
            //will be used to escape the for-loop
            m_TotalCharacters = Integer.parseInt(totalCharacterString);

            //get just the characters section of the JSON Object
            JsonArray characterArray = data.getAsJsonArray("results");


            //iterate through the array, pulling out the relevant info
            for(int j = 0; j < characterArray.size(); j++){
                //convert the array element to a JSON object, via a JSON element
                JsonElement character = characterArray.get(j);
                JsonObject charObject = character.getAsJsonObject();

                //get the ID, and convert to a string
                JsonElement id = charObject.get("id");
                String idString = id.toString();

                //get the name, and convert to a string
                JsonElement charName = charObject.get("name");
                String nameString = charName.toString();
                //clean it
                nameString = nameString.replaceAll("^\"|\"$", "");

                //get the description, and convert to a string
                JsonElement charDescr = charObject.get("description");
                String descrString = charDescr.toString();
                //clean
                descrString = descrString.replaceAll("^\"|\"$", "");

                //get the thumbnail url, clean and concat
                JsonObject thumbnail = charObject.getAsJsonObject("thumbnail");
                String path = thumbnail.get("path").toString().replaceAll("^\"|\"$", "") + "." + thumbnail.get("extension").toString().replaceAll("^\"|\"$", "");

                //the url for the character info is a little deeper
                //get the urls array
                JsonArray urlArray = charObject.getAsJsonArray("urls");
                //details is the first entry
                JsonElement detailURL = urlArray.get(0);
                //convert to object
                JsonObject detailURLObj = detailURL.getAsJsonObject();
                //get the url from it
                JsonElement charInfoURL = detailURLObj.get("url");
                //convert to string and clean
                String infoURLString = charInfoURL.toString();
                infoURLString = infoURLString.replaceAll("^\"|\"$", "");

                //make a new instance of the character class and add it to the Character list
                m_CharacterList.add(new Character(idString, nameString, descrString, path, infoURLString));

                //tell main that a character was loaded
                mainActivity.SomeCharactersLoaded();
            }
        }
        //this line is only reached once the above for loop is completed
        m_AllCharsDownloaded = true;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        try {
            GetMarvelData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    //class for storing relevant character info neatly
    public class Character{
        public String CharID;
        public String CharName;
        public String CharDescr;
        public String CharImageURL;
        public String CharInfoURL;

        public Character(String charID, String charName, String charDescr, String charImageURL, String charInfoURL){
            CharID = charID;
            CharName = charName;
            CharDescr = charDescr;
            CharImageURL = charImageURL;
            CharInfoURL = charInfoURL;
        }
    }

    //
    //for main to access
    //
    public int NumberOfDownloadedChars(){
        return m_CharacterList.size();
    }

    public List<Character> getCharacterList() {
        return this.m_CharacterList;
    }
}
