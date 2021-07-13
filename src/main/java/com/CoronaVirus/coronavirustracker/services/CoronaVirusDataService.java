package com.CoronaVirus.coronavirustracker.services;

import com.CoronaVirus.coronavirustracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/*
* GETS INFORMATION FROM CSV FILE AND PARSES IT OVER
* Creates it as a Spring Boot service
* */

@Service
public class CoronaVirusDataService {

   private static String virusUrl = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
    // Creates LIST
    private List<LocationStats> allStats = new ArrayList<>();

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    // HTTP CALL TO WEBSITE TO RETRIEVE INFORMATION
    // Add exceptions
    // Executes method on run
    @PostConstruct

    // Executes every hour as dataset is always updated
    @Scheduled(cron = " * * 1 * * *")

    public void fetchVirusData() throws IOException,InterruptedException {
        // Creates LIST
       List<LocationStats> newStats = new ArrayList<>();

        // Creates instance of HttpClient and http request
        HttpClient client = HttpClient.newHttpClient();

        // The message that is sent by a client to a server is what is known as an HTTP request.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(virusUrl))
                .build();
        HttpResponse<String> httpResponse = client.send(request,HttpResponse.BodyHandlers.ofString());

       // System.out.println(httpResponse.body());

        // We will now need to convert the string virusUrl and parse the important fields from it
        // We will be able to remove certain parts from the csv like the location and time etc...
        StringReader csvReader = new StringReader(httpResponse.body());

        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvReader);
        // LOOPS THROUGH OBJECTS
        for (CSVRecord record : records) {
            // we will save this in memory
            LocationStats locationStats = new LocationStats();
            // Sets value to these columns
            locationStats.setState(record.get("Province/State"));
            locationStats.setCountry(record.get("Country/Region"));

            // retrieves last index from list which will be most current
            int latestCases = Integer.parseInt(record.get(record.size() - 1));
            int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
            locationStats.setLatestTotalCases(latestCases);
            locationStats.setDiffFromPrevDay(latestCases - prevDayCases);

            // Replaces any empty state strings with the name of Country


            //prints
            System.out.println(locationStats);

            newStats.add(locationStats);
        }
        this.allStats = newStats;

        /*
        * now we need to render in a UI format
        * */


    }
}
