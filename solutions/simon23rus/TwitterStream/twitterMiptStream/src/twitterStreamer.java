
import com.beust.jcommander.JCommander;
import twitter4j.*;
import twitter4j.util.TimeSpanConverter;

import java.lang.String;
import java.lang.System;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import com.google.code.geocoder.*;
import com.google.code.geocoder.model.*;


public class twitterStreamer {

    private static final short countryCodeSize = 2;
    private static String searchingPlace;
    private static boolean retweetCollapse;
    private static int tweetsLimit;
    private static final int SECONDS_IN_YEAR = 60 * 60 * 24 * 365;
    private static final int SECONDS_IN_MONTH = 60 * 60 * 24 * 30;
    private static final int SECONDS_IN_DAY = 60 * 60 * 24;
    private static final int SECONDS_IN_HOUR = 60 * 60;
    private static final int SECONDS_IN_MINUTE = 60;
    private static boolean isItYesterday;




    public static String correctRussianText(int deltaInSeconds) {
        if(deltaInSeconds < 2 * SECONDS_IN_MINUTE)
            return "[<Только что>] ";
        else if(deltaInSeconds < SECONDS_IN_HOUR) {
            int minutes = deltaInSeconds / SECONDS_IN_MINUTE;
            if(minutes >= 15 || minutes <= 10) {
                if (minutes % 10 == 1)
                    return "[<" + minutes + " минуту назад>] ";
                else if (minutes % 10 == 2 || minutes % 10 == 3 || minutes % 10 == 4)
                    return "[<" + minutes + " минуты назад>] ";
                else
                    return "[<" + minutes + " минут назад>] ";
            }
            else
                return "[<" + minutes + " минут назад>] ";
        }

        else if(isItYesterday)
            return "[<вчера>] ";

        else if(deltaInSeconds < 24 * SECONDS_IN_HOUR) {
            int hours = deltaInSeconds / SECONDS_IN_HOUR;
            if (hours >= 15 || hours <= 10) {
                if (hours % 10 == 1)
                    return "[<" + hours + " час назад>] ";
                else if (hours % 10 == 2 || hours % 10 == 3 || hours % 10 == 4)
                    return "[<" + hours + " часа назад>] ";
                else
                    return "[<" + hours + " часов назад>] ";
            } else
                return "[<" + hours + " часов назад>] ";
        }
        else {
            int days = deltaInSeconds / SECONDS_IN_DAY;
            if (days >= 15 || days <= 10) {
                if (days % 10 == 1)
                    return "[<" + days + " день назад>] ";
                else if (days % 10 == 2 || days % 10 == 3 || days % 10 == 4)
                    return "[<" + days + " дня назад>] ";
                else
                    return "[<" + days + " дней назад>] ";
            } else
                return "[<" + days + " дней назад>] ";
        }
    }

    public static StatusListener listener = new StatusListener(){
        public void onStatus(Status givenTweet) {
            printStringWithFormat(givenTweet, false);
            try {
                Thread.sleep(1000);                 //1000=1.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
        public void onException(Exception ex) {
            //ex.printStackTrace();
        }
        @Override
        public void  onStallWarning(StallWarning x){}
        @Override
        public void onScrubGeo(long first, long second){}
    };

    public static int getDeltaInTime(Status tweet) {
        TimeSpanConverter converter = new TimeSpanConverter();
        System.out.println(converter.toTimeSpanString(tweet.getCreatedAt()));

        Date currentDate = Calendar.getInstance().getTime();

        Date tweetDate = tweet.getCreatedAt();
        String currentYear = new SimpleDateFormat("yy").format(currentDate);
        String currentMonth = new SimpleDateFormat("MM").format(currentDate);
        String currentDay = new SimpleDateFormat("dd").format(currentDate);
        String currentHour = new SimpleDateFormat("HH").format(currentDate);
        String currentMinute = new SimpleDateFormat("mm").format(currentDate);
        String currentSecond = new SimpleDateFormat("ss").format(currentDate);

        long firstSec = currentDate.getTime();
        long tweetSec = tweetDate.getTime();
        System.out.println(firstSec - tweetSec + "d9y3129");

        String tweetYear = new SimpleDateFormat("yy").format(tweetDate);
        String tweetMonth = new SimpleDateFormat("MM").format(tweetDate);
        String tweetDay = new SimpleDateFormat("dd").format(tweetDate);
        String tweetHour = new SimpleDateFormat("HH").format(tweetDate);
        String tweetMinute = new SimpleDateFormat("mm").format(tweetDate);
        String tweetSecond = new SimpleDateFormat("ss").format(tweetDate);

        System.out.println(new SimpleDateFormat("yyyy MM dd hh mm ss").format(currentDate));
        System.out.println(tweetYear + " " + tweetMonth + " " + tweetDay + " " + tweetHour + " " + tweetMinute + " " + tweetSecond);

        int secondsDelta =
                (Integer.valueOf(currentYear) - Integer.valueOf(tweetYear)) * SECONDS_IN_YEAR
                + (Integer.valueOf(currentMonth) - Integer.valueOf(tweetMonth)) * SECONDS_IN_MONTH
                + (Integer.valueOf(currentDay) - Integer.valueOf(tweetDay)) * SECONDS_IN_DAY
                + (Integer.valueOf(currentHour) - Integer.valueOf(tweetHour)) * SECONDS_IN_HOUR
                + (Integer.valueOf(currentMinute) - Integer.valueOf(tweetMinute)) * SECONDS_IN_MINUTE
                + Integer.valueOf(currentSecond) - Integer.valueOf(tweetSecond);

        System.out.println(secondsDelta);

        int todaySeconds = Integer.valueOf(currentSecond) + Integer.valueOf(currentMinute) * SECONDS_IN_MINUTE + Integer.valueOf(currentHour) * SECONDS_IN_HOUR;
        System.out.println(todaySeconds);
        if(secondsDelta < SECONDS_IN_DAY + todaySeconds  && secondsDelta > todaySeconds)
            isItYesterday = true;
        else
            isItYesterday = false;

        return secondsDelta;
    }


    public static void printStringWithFormat(Status tweet, boolean timeIsNeeded) {
        String tweetToShow = "";
        System.out.println(tweet.getText() + "etotext\n");
        if(timeIsNeeded) {
            int delta = getDeltaInTime(tweet);
            tweetToShow += (char) 27 + "[35;1;4m" + correctRussianText(delta);
        }
            tweetToShow += (char) 27 + "[34;1m@" + tweet.getUser().getScreenName();
            if(tweet.isRetweet())
                tweetToShow += ":" + (char) 27 + "[33;4m" + " ретвитнул " + (char) 27 + "[31;1m@" + tweet.getRetweetedStatus().getUser().getScreenName() +  (char) 27 + "[0m" + ":" + tweet.getText().substring(5 + tweet.getRetweetedStatus().getUser().getScreenName().length());
            else
                tweetToShow += (char) 27 + "[0m" + ":" + tweet.getText();
            if(!tweet.isRetweet())
                tweetToShow += (char) 27 + "[42m" + "(<" + tweet.getRetweetCount() + "> Ретвитов)" + (char) 27 + "[0m";
            System.out.println(tweetToShow);


    }



    private static String webSource() throws IOException, JSONException {
        URL newUrl = new URL("http://ip-api.com/json");
        URLConnection urlConnecter = newUrl.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                urlConnecter.getInputStream(), "UTF-8"));
        JSONObject givenSource = new JSONObject(in.readLine());
        StringBuilder mySourse = new StringBuilder();
        mySourse.append(givenSource.getString("city"));
        in.close();
        return mySourse.toString();
    }

    public static GeoLocation convertToGeoCode(String thisPlace) throws IOException, JSONException {
        if ("nearby".equals(thisPlace)) {
            thisPlace = webSource();
            System.out.println("thisplac" + thisPlace);
        }
        try {
            final Geocoder geocoder = new Geocoder();
            GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().setAddress(thisPlace).getGeocoderRequest();
            GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
            List<GeocoderResult> geocoderResult = geocoderResponse.getResults();
            float latitude = geocoderResult.get(0).getGeometry().getLocation().getLat().floatValue();
            float longitude = geocoderResult.get(0).getGeometry().getLocation().getLng().floatValue();
            System.out.printf("%f, \n %f", latitude, longitude);
            GeoLocation gl = new GeoLocation(latitude, longitude);
            return gl;
        } catch (Exception ge) {
            System.out.println("Error in Geocoder: " + ge.getMessage());
        }
        return null;
    }




    public static void main(String[] args) throws TwitterException, IOException, InterruptedException, JSONException {

        myJCommander jct = new myJCommander();
        new JCommander(jct, args);

        if(jct.getClearQuery().equals(null) || jct.help) {
            System.out.println("Now U R getting hints 4 usage dat" +
                    "\n[--query|-q <query or keywords for stream>]\n" +
                    "[--place|-p <location|'nearby'>]\n" +
            "[--stream|-s]\n" +
            "[--hideRetweets]\n" +
            "[--limit|-l <tweets>]\n" +
            "[--help|-h]\n");
            return;
        }

        Twitter twitter = new TwitterFactory().getInstance();
        if(jct.toPost != null)
            twitter.updateStatus(jct.toPost);
        searchingPlace = jct.place;
        retweetCollapse = jct.hideRetweets;
        tweetsLimit = jct.tweetsByQuery;


        System.out.println("It is your searching place  " + searchingPlace);

        System.out.println("city, where you are surfing now " + webSource());

        if(jct.stream) {
            TwitterStream myTwitterStream = new TwitterStreamFactory().getInstance();
            myTwitterStream.addListener(listener);
            FilterQuery myFilter = new FilterQuery();
            String[] toTrack = {jct.query};
            myFilter.track(toTrack);
            myTwitterStream.filter(myFilter);
        }
        else {
            if(retweetCollapse)
                jct.query += " +exclude:retweets";
            Query query = new Query(jct.query);
            QueryResult result;
            query.setCount(tweetsLimit);
            if(!searchingPlace.equals("aroundtheworld"))
                query.setGeoCode(convertToGeoCode(searchingPlace), 40, Query.Unit.km);
            int counter = 0;
            do {
                result = twitter.search(query);
                List<Status> tweetsFound = result.getTweets();
                for(Status tweet : tweetsFound) {
                        printStringWithFormat(tweet, true);
//                        System.out.println(tweet.getPlace());
                        ++counter;
                        if (counter == tweetsLimit)
                            return;
                }
            } while ((counter < tweetsLimit) && (query = result.nextQuery()) != null);
        }

    }

}