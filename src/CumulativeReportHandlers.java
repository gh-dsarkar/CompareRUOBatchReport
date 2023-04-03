import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class CumulativeReportHandlers {

    private static String[] RECORD_INFO  ;
    //    static String Filepath = "Resources/Sample.csv";
    static String Filepath = "Resources/testdata.json";
    public static HashMap <String, ArrayList<Map>  > AllResults = new HashMap<>();
    public static ArrayList<Map>   CNV= new ArrayList<>();
    public static ArrayList<Map>   SNV= new ArrayList<>();
    public static ArrayList<Map>   Fusion = new ArrayList<>();
    public static ArrayList<Map>   DenovoFusion = new ArrayList<>();
    public static ArrayList<Map>   Indel = new ArrayList<>();
    public static ArrayList<Map>   Deletion = new ArrayList<>();
    public static ArrayList<Map>   AlleleType = new ArrayList<>();
    public static ArrayList<Map>   SingleRegionMethylCall= new ArrayList<>();
    public static ArrayList<Map>   Virus = new ArrayList<>();
    public static ArrayList<Map>   SampleMethyl = new ArrayList<>();
    public static ArrayList<Map>   TMB = new ArrayList<>();
    public static ArrayList<Map>   HRD = new ArrayList<>();
    public static ArrayList<Map>   MSI = new ArrayList<>();

    public static   HashMap<String, HashMap<String,ArrayList<Map>>> CumulativeArrayLst;

    public static int lineno;




    public static final String COMMA_DELIMITER =",";

    private static  String[] RECORD_HEADERS ;



//    public static void main(String args[]) throws Exception {
//
//        readCSVReport();
//
//    }

    public static HashMap<String,ArrayList<Map>> readFile(){
        HashMap<String,ArrayList<Map>> tset = new HashMap<>();
        return tset;
    }

    private static void useReader(){

    }

    public static HashMap<String, HashMap<String, ArrayList<Map>>> readCumulativeReport(String reportLocation, String sampleID) throws FileNotFoundException {

        try (BufferedReader br = new BufferedReader(new FileReader(reportLocation))) {
            String line;
             lineno =0;


            while ((line = br.readLine()) != null) {
//               for (String sampleID: sampleIDlst) {
                   if (lineno == 0) {
                       String temp = line.replaceAll("\"", "");
                       RECORD_HEADERS = temp.split(COMMA_DELIMITER);
                       lineno++;
                   } else {
                       String temp = line.replaceAll("\"", "");
                       String[] values = temp.split(COMMA_DELIMITER);

                       if (values[4].equalsIgnoreCase(sampleID)) {



                           switch (values[9]) {
                               case "CNV":
//                            if ((values[31].contains("deletion"))) {
                                   if ((values[31].equalsIgnoreCase("loh")) || (values[31].equalsIgnoreCase("homdel"))) {
                                       Deletion.add(ObjectBuilder(values));
                                       break;
                                   } else {
                                       CNV.add(ObjectBuilder(values));
                                       break;
                                   }
                               case "SNV":
                                   SNV.add(ObjectBuilder(values));
                                   break;
                               case "Fusion":
                                   Fusion.add(ObjectBuilder(values));
                                   break;
                               case "LGR":
                                   DenovoFusion.add(ObjectBuilder(values));
                                   break;
                               case "Indel":
                                   Indel.add(ObjectBuilder(values));
                                   break;
                               case "AlleleType": //AlleleType
                                   AlleleType.add(ObjectBuilder(values));
                                   break;
                               case "PromoterMethylation":
                                   SingleRegionMethylCall.add(ObjectBuilder(values));
                                   break;

                               case "Virus":
                                   Virus.add(ObjectBuilder(values));
                                   break;
                               case "SampleLevelMethylation":
                                   SampleMethyl.add(ObjectBuilder(values));
                                   break;
                               case "TMB":
                                   TMB.add(ObjectBuilder(values));
                                   break;
                               case "HRD":
                                   HRD.add(ObjectBuilder(values));
                                   break;
                               case "MSI":
                                   MSI.add(ObjectBuilder(values));
                                   break;

                           }
                           lineno++;


                       }
                   }


                   System.out.println("No of CNVrecords :" + CNV.size());
                   System.out.println("No of SNVrecords :" + SNV.size());
                   System.out.println("No of Fusionrecords :" + Fusion.size());
                   System.out.println("No of DenovoFusionrecords :" + DenovoFusion.size());
                   System.out.println("No of Indelrecords :" + Indel.size());
                   System.out.println("No of Deletionrecords :" + Deletion.size());
                   System.out.println("No of AlleleTyperecords :" + AlleleType.size());
                   System.out.println("No of SingleRegionMethylCallrecords :" + SingleRegionMethylCall.size());
                   System.out.println("No of Virusrecords :" + Virus.size());
                   System.out.println("No of SampleMethylrecords :" + SampleMethyl.size());
                   System.out.println("No of TMBrecords :" + TMB.size());
                   System.out.println("No of HRDrecords :" + HRD.size());
                   System.out.println("No of MSIrecords :" + MSI.size());

                   AllResults.put("CNV", CNV);

                   AllResults.put("SNV", SNV);
                   AllResults.put("Fusion", Fusion);
                   AllResults.put("DenovoFusion", DenovoFusion);
                   AllResults.put("Indel", Indel);
                   AllResults.put("Deletion", Deletion);
                   AllResults.put("AlleleType", AlleleType);
                   AllResults.put("SingleRegionMethylCall", SingleRegionMethylCall);
                   AllResults.put("Virus", Virus);
                   AllResults.put("SampleMethyl", SampleMethyl);
                   AllResults.put("TMB", TMB);
                   AllResults.put("HRD", HRD);
                   AllResults.put("MSI", MSI);


                   CumulativeArrayLst.put(sampleID, AllResults);
//               }
            }

        }
        catch (IOException e)
        {
            e.getMessage();
            e.printStackTrace();
        }
        return CumulativeArrayLst;
    }

    public static void readJSONData(){
        try {
            String eachRecord ="";
            JSONParser jsonParser = new JSONParser();
            FileReader reader = new FileReader(Filepath);
            //Read JSON file
            Object obj = jsonParser.parse(reader);
            JSONArray allObj = (JSONArray) obj;
            for (Object eachObj :  allObj) {
                eachRecord = eachObj.toString();
                JsonObject jsonObject = new JsonParser().parse(eachRecord).getAsJsonObject();
                String VariantType= jsonObject.get("Variant_type").toString();
                VariantType = VariantType.substring(1,VariantType.length()-1);
                if (VariantType.equals("CNV")) {
                    String Gene = jsonObject.get("Gene").toString();
                    System.out.println(Gene);
                };
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }

    private static Map<String,String> ObjectBuilder( String[] valuesLists){
        Map<String,String> objectBuilder = new HashMap<>();
        for (int i=0;i<RECORD_HEADERS.length;i++){
            objectBuilder.put(RECORD_HEADERS[i],valuesLists[i]);
        }
    return objectBuilder;
    }

//    public static HashMap<String,ArrayList<Map>> readCumulativeCSVReport(String reportLocation,String SampleIDs){
//
////        while
//
//    }

}
