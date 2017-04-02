package nearsoft.academy.bigdata.recommendation;


import java.io.*;
import java.util.*;

import com.google.common.collect.HashBiMap;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

/**
 * Created by Noe on 3/29/17.
 */

public class MovieRecommender {
    private HashBiMap<String, Integer> products = HashBiMap.create();
    private HashMap<String, Integer> users = new HashMap<String, Integer>();
    private DataModel dataModel;
    private UserSimilarity similarity;
    private UserNeighborhood neighborhood;
    private UserBasedRecommender recommender;
    private int numberOfReviews = 0;
    private String parsedFile = "parsed.txt";
    private void parseFile(String path) {
        try {
            PrintWriter writer = new PrintWriter(parsedFile, "UTF-8");
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith("product/productId:")) {
                    numberOfReviews++;
                    String product = line.split(" ")[1];
                    String user = "", score = "";
                    line = reader.readLine();
                    while (!line.startsWith("review/text:") && line != null) {
                        if (line.startsWith("review/userId:"))
                            user = line.split(" ")[1];
                        else if (line.startsWith("review/score:"))
                            score = line.split(" ")[1];
                        line = reader.readLine();
                    }
                    if (!products.containsKey(product)) {
                        products.put(product, products.size());
                    }
                    if (!users.containsKey(user))
                        users.put(user, users.size());
                    writer.write(users.get(user) + "," + products.get(product) + "," + score + "\n");
                }
                line = reader.readLine();
            }
            reader.close();
            writer.close();
            System.out.println("Done parsing!");
        } catch (Exception e) {
            System.out.println("something is wrong");
        }
    }
    public MovieRecommender(String path) {
        parseFile(path);
        try {
            dataModel = new FileDataModel(new File(parsedFile));
            similarity = new PearsonCorrelationSimilarity(dataModel);
            neighborhood = new ThresholdUserNeighborhood(0.1, similarity, dataModel);
            recommender = new GenericUserBasedRecommender(dataModel, neighborhood, similarity);
        }
        catch (Exception e) {
            System.out.println("Something is wrong...");
        }
    }

    public int getTotalReviews() {
        return numberOfReviews;
    }

    public int getTotalProducts() {
        return products.size();
    }

    public int getTotalUsers() {
        return users.size();
    }
    public List<String> getRecommendationsForUser(String user) {
        try {
            List<RecommendedItem> recommendation = recommender.recommend(users.get(user), 3);
            List<String> output = new ArrayList<String>();
            for (RecommendedItem r : recommendation)
                output.add(products.inverse().get((int)r.getItemID()).toString());
            return output;
        }
        catch(Exception e){
            System.out.println("Something is wrong :p");
            return null;
        }
    }
}