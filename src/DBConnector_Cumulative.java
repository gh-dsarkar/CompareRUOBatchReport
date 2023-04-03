import com.opencsv.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.sql.*;
import java.text.*;
import java.util.Date;
import java.util.*;

public class DBConnector_Cumulative {
    public static Connection con;
    private static ResultSetMetaData rsmd;

    private static ArrayList<Map> CNV;

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

    public static HashMap<String,String> resultMap = new HashMap<>();

//    public static HashMap<String,ArrayList<String>> reportType_IO = new HashMap<>();
//    public static HashMap<String,ArrayList<String>> reportType_HRD = new HashMap<>();
//    public static HashMap<String,ArrayList<String>> reportType_Base = new HashMap<>();
//    public static HashMap<String,ArrayList<String>> reportType_Methylation = new HashMap<>();
//    public static HashMap<String,ArrayList<String>> reportType_Virus = new HashMap<>();

    public static HashMap<String,ArrayList<String>> reportType_1 = new HashMap<>();
    public static HashMap<String,ArrayList<String>> reportType_2 = new HashMap<>();
    public static HashMap<String,ArrayList<String>> reportType_3 = new HashMap<>();
    public static HashMap<String,ArrayList<String>> reportType_4 = new HashMap<>();
    public static HashMap<String,ArrayList<String>> reportType_5 = new HashMap<>();
    public static HashMap<String,ArrayList<String>> reportType_6 = new HashMap<>();
    public static HashMap<String,ArrayList<String>> reportType_7 = new HashMap<>();
    public static HashMap<String,ArrayList<String>> reportType_8 = new HashMap<>();

    public static HashMap<String,ArrayList<String>> reportType_9 = new HashMap<>();

    public static HashMap<String,ArrayList<String>> reportType_10 = new HashMap<>();

    public static HashMap<String,ArrayList<String>> reportType_11 = new HashMap<>();

    public static HashMap<String,ArrayList<String>> reportType_12 = new HashMap<>();

    public static HashMap<String,ArrayList<String>> reportType_13 = new HashMap<>();

    public static HashMap<String,ArrayList<String>> reportType_14 = new HashMap<>();

    public static HashMap<String,ArrayList<String>> reportType_15 = new HashMap<>();
    public static String ReportType;

    private static final String LINE_SEPARATOR = System.getProperty("line.separator","\n");
    private static final String DEFAULT_DELIMITER = ";";
    private static String delimiter = DEFAULT_DELIMITER;

    private static String CNV_type;

    private static ResultSet results;

    private static int reportOption;

    private static String reportLocation;

    private static  String resultsFileLocation ="Results";

    private static  String errorFileLocation ="Errors";

    private static int counter=1;
    private static FileWriter errorfileWriter;
    private static String SampleIDInput;
    private static int envOption;
    private static boolean isTrue=true;
//    private static MappingIterator<Object> userinput;


    //    @Test
    public static void ConnectionSetUp()
    {
        try {




            int noOfRecords=0;

            handlingReportType();
            String username;
            String password ;
            String hostname ;
            String port ;
            String SID ;

            String DB_LIMS_Schema;
            String DB_ServiceName;
            String connectionString ;
            try{
                username = "dsarkar";
                password = "Ventana@27gh";
                hostname = "gh-val-db-lims.ghdna.io";
                port = "1521";
                SID = "limsval";
//                --------
                DB_LIMS_Schema = "labvantage";
                DB_ServiceName = "ocrl";
//                ---------
                connectionString = "";
                if (!SID.equals("")) {
                    connectionString = "jdbc:oracle:thin:@" + hostname + ":" + port + ":" + SID;
                } else if (!DB_ServiceName.equals("")) {
                    connectionString = "jdbc:oracle:thin:@//" + hostname + ":" + port + "/" + DB_ServiceName;
                }
                System.out.println(connectionString);
                try {
                    Class.forName("oracle.jdbc.driver.OracleDriver");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                try {
                    con = DriverManager.getConnection(
                            connectionString, username,
                            password);

//                    con.setSchema(DB_LIMS_Schema);
                    System.out.println("Connected to Database succesfully");
                } catch (Exception e) {
                    e.getMessage();
                }

                //Read records from db

                Statement st = con.createStatement(
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY
                );

                st.executeQuery("ALTER SESSION SET CURRENT_SCHEMA = labvantage");

                //Getting Gene Information applicable for Report Types
                getApplicableGeneInfo(st);


                // Report Data
                ReportHandlers rh = new ReportHandlers();
                HashMap<String, ArrayList<Map>> reportMap = rh.readCSVReport(reportLocation);
//              MAKE CHANGES HERE WRT TO THE STORED PROCEDURE
                BufferedReader in = new BufferedReader(new FileReader("Resources/CumulativeRUOReportStoredProcedure.sql"));

                StringBuffer sb = new StringBuffer();
                String command = readLineByLine(in);
//                Creating new file to export data from LIMS DB to CSV


                Date date = Calendar.getInstance().getTime();
                DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm");
                String strDate = dateFormat.format(date);


                String exportedFilename = "DB_Output_" + ReportType + "_" + strDate + ".csv";
                String exportFileLocation = "Reports";
                File file = new File(exportFileLocation + "//" + exportedFilename);

                CSVWriter fileWriter = new CSVWriter(new FileWriter(exportFileLocation + "//" + exportedFilename));
                File f = new File(exportFileLocation);


                if (!f.exists()) {
                    f.mkdir();
                }
                file.createNewFile();

                String tmpCommand = command.replaceAll("%s", SampleIDInput);
                results = st.executeQuery(tmpCommand);

                fileWriter.writeAll(results, true);
                fileWriter.close();


                File errorReportLocation = new File(errorFileLocation);
                String ErrorFilename = "Error_" + ReportType + "_" + strDate + ".txt";
                File errorfile = new File(errorFileLocation + "//" + ErrorFilename);
                if (!errorReportLocation.exists()) {
                    errorReportLocation.mkdir();
                }
                errorfile.createNewFile();
                errorfileWriter = new FileWriter(errorfile);


//                Get Records Count
                if (results.last()) {
                    noOfRecords = results.getRow();
                } else {
                    noOfRecords = 0;
                }
                System.out.println("Number of records in DB is " + noOfRecords);

                //Set pointer to first record
                results.first();


                // For each row of the result set ...
                while (results.next()) {
                    rsmd = results.getMetaData();
                    int dbColumns = rsmd.getColumnCount();
                    for (int i = 1; i < dbColumns + 1; i++) {
                        String values = results.getString(i);
                        if (results.getString(i) == null) {
                            values = "";
                        }


                        //Transformation Rules
                        //Rule 1 : Base Report : No HRD Data
                        //Applicable Variants : CNV,SNV	Fusion	DenovoFusion	Indel	Deletion	AlleleType	SingleRegionMethylCall	Virus

                        values = handlingTransformationRules(reportOption, i, values);

                        resultMap.put(rsmd.getColumnName(i).toString(), values);


                    }

                    String variant_type = results.getString("Variant_type");


                    CNV_type = results.getString("CNV_type");
                    String Position = results.getString("Position");


                    switch (variant_type) {
                        case "CNV":
                            Handling_CNV_Deletions_Data(reportMap, resultMap);
                            break;
                        case "SNV":
                            Handling_SNV_Data(reportMap, resultMap);
                            break;
                        case "Fusion":
                            Handling_Fusion_Data(reportMap, resultMap);
                            break;
                        case "LGR":
                            Handling_DenovoFusion_Data(reportMap, resultMap);
                            break;
                        case "Indel":
                            Handling_Indel_Data(reportMap, resultMap);
                            break;

                        case "AlleleType":
                            Handling_AlleleType_Data(reportMap, resultMap);
                            break;
                        case "PromoterMethylation":
                            Handling_SingleRegionMethylCall_Data(reportMap, resultMap);
                            break;
                        case "Virus":
                            Handling_Virus_Data(reportMap, resultMap);
                            break;
                        case "SampleLevelMethylation":
                            Handling_SampleMethyl_Data(reportMap, resultMap);
                            break;
                        case "TMB":
                            Handling_TMB_Data(reportMap, resultMap);
                            break;
                        case "HRD":
                            Handling_HRD_Data(reportMap, resultMap);
                            break;
                        case "MSI":
                            Handling_MSI_Data(reportMap, resultMap);
                            break;
                    }
                    counter++;

                }
//              for (ArrayList<Map> ar :reportMap.values() { if (ar.size()>0){
//                System.out.println("Check records for Variant type");
//            } else{
//            }
                System.out.println("Record in Batch Report(input file) :"+rh.lineno);
                System.out.println("Records tested : "+counter);

                if (rh.lineno!= counter || isTrue==false
                ){
                    System.err.println("Check the Project report type in Project Configuration");
                    System.err.println("Verification failed");
                }
                else{
                    System.err.println("\033[1;32m" + "Verification passed"+"\u001B[0m");
                }
                errorfileWriter.close();


                //Delete if file is empty
                if (errorfile.length()==0){
                    errorfile.delete();
                }
                st.close();

            } catch (Exception e) {
                e.getMessage();
                e.printStackTrace();
            }

            if (con == null) {

                throw new Exception("ERROR: Connection cannot be established.");
            }


        }
        catch (Exception e)
        {

            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        ConnectionSetUp();

    }


//    private static Map<String,String> fetchReportData(){
//        return Map;
//    };

    private static String readLineByLine(BufferedReader reader) {
        StringBuffer command = new StringBuffer();
        try {
            BufferedReader lineReader = new BufferedReader(reader);
            String line;
            int l =0;

            while ((line = lineReader.readLine()) != null) {
                command = handleLine(command, line);
                l++;
                if (command==null && l!=0){
                    System.out.println("Line Number = "+l);
                }
            }
//            checkForMissingLineTerminator(command);
            return command.toString();

        } catch (Exception e) {
            String message = "Error executing: " + command + ".  Cause: " + e;
//            throw new RuntimeSqlException(message, e);
            e.printStackTrace();
        }
        return command.toString();
    }


    private static void Handling_CNV_Deletions_Data(HashMap<String, ArrayList<Map>> reportMap,HashMap<String,String> resultMap) throws IOException {
        boolean found=false;
//        if (CNV_type.contains("deletion"))
        if (CNV_type.equalsIgnoreCase("loh") || CNV_type.equalsIgnoreCase("homdel") ){
            Deletion = reportMap.get("Deletion");
            for (int i = 0; i < Deletion.size(); i++) {
                if (
                        (resultMap.get("Gene").equals(Deletion.get(i).get("Gene"))) &&
                        (resultMap.get("Chromosome").equals(Deletion.get(i).get("Chromosome"))) &&
                        (resultMap.get("CNV_type").equals(Deletion.get(i).get("CNV_type")))
                ) {
                    if (compareRecords(resultMap, (HashMap<String, String>) Deletion.get(i))){
                        Deletion.remove(i);
                        found=true;
                    };
                }

            }
            if (found=false) {
                System.out.println("CNV Data not found for Gene: "+resultMap.get("Gene").toString());
            }
        }
        else{
            CNV = reportMap.get("CNV");

            for (int i = 0; i < CNV.size(); i++)
                {
                    //New LINE Added

                        if ((resultMap.get("Gene").equals(CNV.get(i).get("Gene"))) &&
                                (resultMap.get("CNV_type").equals(CNV.get(i).get("CNV_type"))) &&
                                (resultMap.get("Chromosome").equals(CNV.get(i).get("Chromosome")))

                        ) {
                                compareRecords(resultMap,(HashMap<String, String>) CNV.get(i));
                                CNV.remove(i);
                                found=true;
                        }


            }
            if (found=false) {
                System.out.println("CNV Data not found for Gene: "+resultMap.get("Gene").toString());
            }
        }
    }





    private static void Handling_SNV_Data(HashMap<String, ArrayList<Map>> reportMap,HashMap<String,String> resultMap){
        boolean found=false;
        SNV = reportMap.get("SNV");
        for (int i = 0; i < SNV.size(); i++) {
            if ((resultMap.get("Gene").equals(SNV.get(i).get("Gene"))) &&
                    (resultMap.get("Position").equals(SNV.get(i).get("Position"))) &&
            (resultMap.get("Chromosome").equals(SNV.get(i).get("Chromosome"))) &&
                    (resultMap.get("Position").equals(SNV.get(i).get("Position"))) &&
            (resultMap.get("Mut_nt").equals(SNV.get(i).get("Mut_nt")))

            ) {
                try {
                    if (compareRecords(resultMap, (HashMap<String, String>) SNV.get(i))){
                        SNV.remove(i);
                        found=true;
                    }
                } catch (Exception e) {
                   e.printStackTrace();
                }
                ;
            }

        }
        if (found=false) {
            System.out.println("SNV Data not found for Gene: "+resultMap.get("Gene").toString());
        }
    }



    private static void Handling_Fusion_Data(HashMap<String, ArrayList<Map>> reportMap,HashMap<String,String> resultMap){
        boolean found=false;
        Fusion = reportMap.get("Fusion");
        for (int i = 0; i < Fusion.size(); i++) {
                if ((resultMap.get("Gene").equals(Fusion.get(i).get("Gene"))) && (resultMap.get("Fusion_gene_b").equals(Fusion.get(i).get("Fusion_gene_b")))) {
                try {
                    if (compareRecords(resultMap, (HashMap<String, String>) Fusion.get(i))){
                        Fusion.remove(i);
                        found=true;
                    }
                } catch (Exception e) {
                   e.getMessage();
                }
                ;
            }

        }
        if (found=false) {
            System.out.println("Fusion Data not found for Gene: "+resultMap.get("Gene").toString());
        }

    }

    private static void Handling_DenovoFusion_Data(HashMap<String, ArrayList<Map>> reportMap,HashMap<String,String> resultMap){
        boolean found=false;
        DenovoFusion = reportMap.get("DenovoFusion");
        for (int i = 0; i < DenovoFusion.size(); i++) {
            if ((resultMap.get("Gene").equals(DenovoFusion.get(i).get("Gene"))) && (resultMap.get("Position").equals(DenovoFusion.get(i).get("Position")))) {
                try {
                    if (compareRecords(resultMap, (HashMap<String, String>) DenovoFusion.get(i))){
                        DenovoFusion.remove(i);
                        found=true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ;
            }

        }
        if (found=false) {
            System.out.println("DenovoFusion Data not found for Gene: "+resultMap.get("Gene").toString());
        }

    }

    public static void Handling_Indel_Data(HashMap<String, ArrayList<Map>> reportMap,HashMap<String,String> resultMap){
        boolean found=false;
        Indel = reportMap.get("Indel");
        for (int i = 0; i < Indel.size(); i++) {
            if ((resultMap.get("Gene").equals(Indel.get(i).get("Gene"))) && (resultMap.get("Position").equals(Indel.get(i).get("Position")))) {
                try {
                    if (compareRecords(resultMap, (HashMap<String, String>) Indel.get(i))){
                        Indel.remove(i);
                        found=true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ;
            }

        }
        if (found=false) {
            System.out.println("Indel Data not found for Gene: "+resultMap.get("Gene").toString());
        }
    }

    private static void Handling_AlleleType_Data(HashMap<String, ArrayList<Map>> reportMap,HashMap<String,String> resultMap){
        boolean found=false;
        AlleleType = reportMap.get("AlleleType");
        for (int i = 0; i < AlleleType.size(); i++) {
            if ((resultMap.get("Gene").equals(AlleleType.get(i).get("Gene"))) && (resultMap.get("Position").equals(AlleleType.get(i).get("Position")))) {
                try {
                    if (compareRecords(resultMap, (HashMap<String, String>) AlleleType.get(i))){
                        AlleleType.remove(i);
                        found=true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ;
            }

        }
        if (found=false) {
            System.out.println("AlleleType Data not found for Gene: "+resultMap.get("Gene").toString());
        }
    }



    private static void Handling_SingleRegionMethylCall_Data(HashMap<String, ArrayList<Map>> reportMap,HashMap<String,String> resultMap){
        boolean found=false;

        SingleRegionMethylCall = reportMap.get("SingleRegionMethylCall");
        for (int i = 0; i < SingleRegionMethylCall.size(); i++) {
            if ((resultMap.get("Gene").equals(SingleRegionMethylCall.get(i).get("Gene"))) && (resultMap.get("Position").equals(SingleRegionMethylCall.get(i).get("Position")))) {
                try {
                    if (compareRecords(resultMap, (HashMap<String, String>) SingleRegionMethylCall.get(i))){
                        SingleRegionMethylCall.remove(i);
                        found=true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ;
            }

        }
        if (found=false) {
            System.out.println("SingleRegionMethylCall Data not found for Gene: "+resultMap.get("Gene").toString());
        }

    }

    private static void Handling_Virus_Data(HashMap<String, ArrayList<Map>> reportMap,HashMap<String,String> resultMap) throws IOException {
        boolean found=false;
        Virus = reportMap.get("Virus");
        for (int i = 0; i < Virus.size(); i++) {
            if ((resultMap.get("Gene").equals(Virus.get(i).get("Gene")))) {
                if (compareRecords(resultMap, (HashMap<String, String>) Virus.get(i))){
                    Virus.remove(i);
                    found=true;
                };
            }

        }
        if (found=false) {
            System.out.println("Virus Data not found for Gene: "+resultMap.get("Gene").toString());
        }

    }

    private static void Handling_SampleMethyl_Data(HashMap<String, ArrayList<Map>> reportMap,HashMap<String,String> resultMap){
        boolean found=false;
        SampleMethyl = reportMap.get("SampleMethyl");
        for (int i = 0; i < SampleMethyl.size(); i++) {
            if ((resultMap.get("Gene").equals(SampleMethyl.get(i).get("Gene"))) && (resultMap.get("Position").equals(SampleMethyl.get(i).get("Position")))) {
                try {
                    if (compareRecords(resultMap, (HashMap<String, String>) SampleMethyl.get(i))){
                        SampleMethyl.remove(i);
                        found=true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ;
            }

        }
        if (found=false) {
            System.out.println("SampleMethyl Data not found for Gene: "+resultMap.get("Gene").toString());
        }
    }



    private static void Handling_TMB_Data(HashMap<String, ArrayList<Map>> reportMap,HashMap<String,String> resultMap){
        boolean found=false;

        TMB = reportMap.get("TMB");
        for (int i = 0; i < TMB.size(); i++) {
            if ((resultMap.get("Gene").equals(TMB.get(i).get("Gene"))) && (resultMap.get("Position").equals(TMB.get(i).get("Position")))) {
                try {
                    if (compareRecords(resultMap, (HashMap<String, String>) TMB.get(i))){
                        TMB.remove(i);
                        found=true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ;
            }

        }
        if (found=false) {
            System.out.println("TMB Data not found for Gene: "+resultMap.get("Gene").toString());
        }

    }
    private static void Handling_HRD_Data(HashMap<String, ArrayList<Map>> reportMap,HashMap<String,String> resultMap){
        boolean found=false;
        HRD = reportMap.get("HRD");
        for (int i = 0; i < HRD.size(); i++) {
            if ((resultMap.get("Gene").equals(HRD.get(i).get("Gene"))) && (resultMap.get("Position").equals(HRD.get(i).get("Position")))) {
                try {
                    if (compareRecords(resultMap, (HashMap<String, String>) HRD.get(i))){
                        HRD.remove(i);
                        found=true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ;
            }

        }
        if (found=false) {
            System.out.println("HRD Data not found for Gene: "+resultMap.get("Gene").toString());
        }

    }


    private static void Handling_MSI_Data(HashMap<String, ArrayList<Map>> reportMap,HashMap<String,String> resultMap){
        boolean found=false;
        MSI = reportMap.get("MSI");
        for (int i = 0; i < MSI.size(); i++) {
            if ((resultMap.get("Gene").equals(MSI.get(i).get("Gene"))) && (resultMap.get("Position").equals(MSI.get(i).get("Position")))) {
                try {
                    if (compareRecords(resultMap, (HashMap<String, String>) MSI.get(i))){
                        MSI.remove(i);
                        found=true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ;
            }

        }
        if (found=false) {
            System.out.println("MSI Data not found for Gene: "+resultMap.get("Gene").toString());
        }

    }

    private static StringBuffer handleLine(StringBuffer command, String line) throws SQLException, UnsupportedEncodingException {
        String trimmedLine = line.trim();
        if (commandReadyToExecute(trimmedLine)) {
            command.append(line.substring(0, line.lastIndexOf(delimiter)));
            command.append(LINE_SEPARATOR);
//            command.setLength(0);
        } else if (trimmedLine.length() > 0) {
            command.append(line);
            command.append(LINE_SEPARATOR);
        }
        return command;
    }

    private static  boolean commandReadyToExecute(String trimmedLine) {
        boolean fullLineDelimiter = false;
        return !fullLineDelimiter && trimmedLine.endsWith(delimiter)
                || fullLineDelimiter && trimmedLine.equals(delimiter);
    }

    private static  void checkForMissingLineTerminator(StringBuffer command) throws Exception {
        if (command != null && command.toString().trim().length() > 0) {
           throw new Exception("Line missing end-of-line terminator (" + delimiter + ") => " + command);

        }
    }

    private static boolean compareRecords(HashMap<String,String> resultHashMap, HashMap<String,String> VariantResultMap) throws IOException {
        ArrayList<String> BaseVariantLists =new ArrayList<>(Arrays.asList("CNV","SNV","Fusion","LGR","Indel","MSI","SNV","TMB"));
        ArrayList<String> IOVariantLists =new ArrayList<>(Arrays.asList("CNV","AlleleType","PromoterMethylation"));
        ArrayList<String> HRDVariantLists =new ArrayList<>(Arrays.asList("HRD","PromoterMethylation"));
        ArrayList<String> methylationVariantLists =new ArrayList<>(Arrays.asList("SampleLevelMethylation"));
        ArrayList<String> VirusVariantLists =new ArrayList<>(Arrays.asList("Virus"));
        switch (reportOption){
            case 1:

                if (!reportType_1.get(VariantResultMap.get("Variant_type")).contains(VariantResultMap.get("Gene"))){
                    if (!reportType_1.get(VariantResultMap.get("Variant_type")).contains("All")) {
                        System.out.println("Check for mismatch with variant:" + VariantResultMap.get("Variant_type") + " variant with GENE Type " + VariantResultMap.get("Gene"));
                        }
                    }


            break;
            case 2:
                if (!reportType_2.get(VariantResultMap.get("Variant_type")).contains(VariantResultMap.get("Gene"))){
                    if (!reportType_2.get(VariantResultMap.get("Variant_type")).contains("All")) {
                        System.out.println("Check for mismatch with variant:" + VariantResultMap.get("Variant_type") + " variant with GENE Type " + VariantResultMap.get("Gene"));
                    }
                }


                break;


            case 3: {
                if (!reportType_3.get(VariantResultMap.get("Variant_type")).contains(VariantResultMap.get("Gene"))){
                    if (!reportType_3.get(VariantResultMap.get("Variant_type")).contains("All")) {
                        System.out.println("Check for mismatch with variant:" + VariantResultMap.get("Variant_type") + " variant with GENE Type " + VariantResultMap.get("Gene"));
                    }
                }

            }
            break;
            case 4:{
                if (!reportType_4.get(VariantResultMap.get("Variant_type")).contains(VariantResultMap.get("Gene"))){
                    if (!reportType_4.get(VariantResultMap.get("Variant_type")).contains("All")) {
                        System.out.println("Check for mismatch with variant:" + VariantResultMap.get("Variant_type") + " variant with GENE Type " + VariantResultMap.get("Gene"));
                    }
                }
            }
            break;
            case 5:
                if (!reportType_5.get(VariantResultMap.get("Variant_type")).contains(VariantResultMap.get("Gene"))){
                    if (!reportType_5.get(VariantResultMap.get("Variant_type")).contains("All")) {
                        System.out.println("Check for mismatch with variant:" + VariantResultMap.get("Variant_type") + " variant with GENE Type " + VariantResultMap.get("Gene"));
                    }
                }
            break;
            case 6:
                if (!reportType_6.get(VariantResultMap.get("Variant_type")).contains(VariantResultMap.get("Gene"))){
                    if (!reportType_6.get(VariantResultMap.get("Variant_type")).contains("All")) {
                        System.out.println("Check for mismatch with variant:" + VariantResultMap.get("Variant_type") + " variant with GENE Type " + VariantResultMap.get("Gene"));
                    }
                }
            break;
            case 7:{



                    if (!reportType_7.get(VariantResultMap.get("Variant_type")).contains(VariantResultMap.get("Gene"))) {
                        if (!reportType_7.get(VariantResultMap.get("Variant_type")).contains("All")) {
                            System.out.println("Check for mismatch with variant:" + VariantResultMap.get("Variant_type") + " variant with GENE Type " + VariantResultMap.get("Gene"));
                        }
                    }

            }
            break;
            case 8:
                if (!reportType_8.get(VariantResultMap.get("Variant_type")).contains(VariantResultMap.get("Gene"))){
                    if (!reportType_8.get(VariantResultMap.get("Variant_type")).contains("All")) {
                        System.out.println("Check for mismatch with variant:" + VariantResultMap.get("Variant_type") + " variant with GENE Type " + VariantResultMap.get("Gene"));
                    }
                }

            break;
            case 9:
                try {
                    if (!reportType_9.get(VariantResultMap.get("Variant_type")).contains(VariantResultMap.get("Gene"))) {
                        if (VariantResultMap.get("Variant_type").equalsIgnoreCase("virus")) {
                            System.out.println("Stop here");
                        }
                        if (!reportType_9.get(VariantResultMap.get("Variant_type")).contains("All")) {
                            System.out.println("Check for mismatch with variant:" + VariantResultMap.get("Variant_type") + " variant with GENE Type " + VariantResultMap.get("Gene"));
                        }
                    }
                }
                catch (Exception e){
                    System.out.println(VariantResultMap.get("Variant_type")+" is causing issue");
                }

            break;

            case 10:
                try {
                    if (!reportType_10.get(VariantResultMap.get("Variant_type")).contains(VariantResultMap.get("Gene"))){
                    if (!reportType_10.get(VariantResultMap.get("Variant_type")).contains("All")) {
                        System.out.println("Check for mismatch with variant:" + VariantResultMap.get("Variant_type") + " variant with GENE Type " + VariantResultMap.get("Gene"));
                    }
                }
            }
                catch (Exception e){
                    System.out.println(VariantResultMap.get("Variant_type")+" is causing issue");
                }
            break;
            case 11:
                try {
                    if (!reportType_11.get(VariantResultMap.get("Variant_type")).contains(VariantResultMap.get("Gene"))){
                        if (!reportType_11.get(VariantResultMap.get("Variant_type")).contains("All")) {
                            System.out.println("Check for mismatch with variant:" + VariantResultMap.get("Variant_type") + " variant with GENE Type " + VariantResultMap.get("Gene"));
                        }
                    }
                }
                catch (Exception e){
                    System.out.println(VariantResultMap.get("Variant_type")+" is causing issue");
                }
                break;
            case 12:
                try {
                    if (!reportType_12.get(VariantResultMap.get("Variant_type")).contains(VariantResultMap.get("Gene"))){
                        if (!reportType_12.get(VariantResultMap.get("Variant_type")).contains("All")) {
                            System.out.println("Check for mismatch with variant:" + VariantResultMap.get("Variant_type") + " variant with GENE Type " + VariantResultMap.get("Gene"));
                        }
                    }
                }
                catch (Exception e){
                    System.out.println(VariantResultMap.get("Variant_type")+" is causing issue");
                }
                break;
            case 13:
                try {
                    if (!reportType_13.get(VariantResultMap.get("Variant_type")).contains(VariantResultMap.get("Gene"))){
                        if (!reportType_13.get(VariantResultMap.get("Variant_type")).contains("All")) {
                            System.out.println("Check for mismatch with variant:" + VariantResultMap.get("Variant_type") + " variant with GENE Type " + VariantResultMap.get("Gene"));
                        }
                    }
                }
                catch (Exception e){
                    System.out.println(VariantResultMap.get("Variant_type")+" is causing issue");
                }
                break;
            case 14:
                try {
                    if (!reportType_14.get(VariantResultMap.get("Variant_type")).contains(VariantResultMap.get("Gene"))){
                        if (!reportType_14.get(VariantResultMap.get("Variant_type")).contains("All")) {
                            System.out.println("Check for mismatch with variant:" + VariantResultMap.get("Variant_type") + " variant with GENE Type " + VariantResultMap.get("Gene"));
                        }
                    }
                }
                catch (Exception e){
                    System.out.println(VariantResultMap.get("Variant_type")+" is causing issue");
                }
                break;
            case 15:
                try {
                    if (!reportType_15.get(VariantResultMap.get("Variant_type")).contains(VariantResultMap.get("Gene"))){
                        if (!reportType_15.get(VariantResultMap.get("Variant_type")).contains("All")) {
                            System.out.println("Check for mismatch with variant:" + VariantResultMap.get("Variant_type") + " variant with GENE Type " + VariantResultMap.get("Gene"));
                        }
                    }
                }
                catch (Exception e){
                    System.out.println(VariantResultMap.get("Variant_type")+" is causing issue");
                }
                break;



        }



            if (resultHashMap.size() != VariantResultMap.size()) {
                System.out.println("Check Reecords for "+resultHashMap.get("Variant_type"));
            }



//Compare DB with Report
//        System.out.println("-----------------------Comparing DB with Report-----------------------------");
//            resultHashMap.forEach((key, value) -> {
//                if (!resultHashMap.get(key).equals(VariantResultMap.get(key))) {
//                    String errorLine = "Report not matching for Variant Type:" + resultHashMap.get("Variant_type") + " and GENE: "
//                            + resultHashMap.get("Gene") + ":  In Report value for " + key + " is :" + VariantResultMap.get(key)
//                            + " in LIMS DB the value is :" + resultHashMap.get(key) + "at line "+counter;
//                    System.err.println(errorLine);
//                    isTrue.set(false);
//                }
//            });



        VariantResultMap.forEach((key, value) -> {
            if (resultHashMap.get("Variant_type").equalsIgnoreCase(VariantResultMap.get("Variant_type")) &&
                    (resultHashMap.get("CNV_type").equalsIgnoreCase(VariantResultMap.get("CNV_Type"))) &&
                    (resultHashMap.get("Chromosome").equalsIgnoreCase(VariantResultMap.get("Chromosome"))) &&
//                    (resultHashMap.get("Position").equalsIgnoreCase(VariantResultMap.get("Position"))) &&
                    (resultHashMap.get("Mut_aa").equalsIgnoreCase(VariantResultMap.get("Mut_aa"))) &&
                    (resultHashMap.get("GHRequestID").equalsIgnoreCase(VariantResultMap.get("GHRequestID")))
            )

//            if (resultHashMap.get("Somatic_status").equalsIgnoreCase("1")){
//                System.out.println("Logged");
//            }


            {
                    if (!resultHashMap.get(key).equals(VariantResultMap.get(key))) {


                            String errorLine = "Report not matching for Variant Type:" + resultHashMap.get("Variant_type") + " and GENE: " + resultHashMap.get("Gene") + ":  In Report value for " + key + " is :" + VariantResultMap.get(key)
                                    + " in LIMS DB the value is :" + resultHashMap.get(key) + " at line " + counter;
                            System.err.println(errorLine);
                            isTrue = false;


                            try {
                                errorfileWriter.write(errorLine);
                                errorfileWriter.write("\n");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                    }

            }
        });
    return isTrue;
    }

//    Variant_type gets set up for CNV call.
//    HRD_SCORE is populated for each row of CNV calls.
//    Only when HRD Module has been ordered.
//    When the HRD module is not ordered, then it should be blank.
//    TMB_score is populated for each row of CNV calls.
//    This is based on TMB data for that sample.
//    TMB_category is populated for each row of CNV calls.
//    This is based on TMB data for that sample.
//    MSI_High is populated for each row of CNV calls.
//    This is based on MSI data for that sample.
//    Tumor_methylation_status
//    Only when Methylation Module has been ordered.
//    Refer to the Confluence page - Sample Methyl
//    When the module is not ordered, then its should be blank.
//    Tumor_methylation_score
//    Only when Methylation Module has been ordered.
//    Refer to the Confluence page - Sample Methyl
//    When the module is not ordered, then its should be blank.


    private static  void handlingReportType() throws URISyntaxException {
        Scanner userinput = new Scanner(System.in);



        System.out.println("Enter the Report location along with the filename:");
        reportLocation = userinput.nextLine().trim();

        if (reportLocation.contains(".")){
            int index = reportLocation.lastIndexOf('.');
            String extension = reportLocation.substring(index + 1);
            File file = new File(reportLocation);
            if (Files.exists(file.toPath()) && extension.equals("csv")){
                System.out.println("Valid filename!");
            }
            else{
                System.out.println("Provide valid CSV REPORT File");
                handlingReportType();
                }
        }
        else{
            System.out.println("Check Filename input");
            handlingReportType();
        }

//        System.out.println("Enter the Sample ID/IDs you want to test against in the report:");
//        System.out.println("For multiple Sample IDs, use ',' Example : B1001,B002");
//         String tmp = "'"+userinput.nextLine()+"'";
//
//        if (tmp.contains(",")){
//            SampleIDInput = String.join(",",tmp.trim().split("\\s*,\\s*"));
//        }
//        else{
//            SampleIDInput= tmp.trim();
//        }

        System.out.println("Select number corresponding to the type of Report generated");
        System.out.println("1. RUO-BASE "); //HRD+Meth Applies
        System.out.println("2. RUO-BASE+HRD "); //Meth Applies 2,4
        System.out.println("3. RUO-BASE+methylation "); // HRD Rules Applied : 3,5
        System.out.println("8. RUO-BASE+IO "); //IO Applies HRD+Meth Applies 1,8
        System.out.println("9. RUO-BASE+Virus ");
        System.out.println("4. RUO-BASE+IO+HRD "); //Meth Applies +IO
        System.out.println("5. RUO-BASE+IO+methylation "); // HRD Rules Applied + IO
        System.out.println("6. RUO-BASE+HRD+methylation "); // Nothing applies IO Applies
        System.out.println("7. RUO-BASE+IO+HRD+methylation "); // Nothing applies
        System.out.println("10. RUO-BASE+IO+HRD+methylation+Virus ");
        System.out.println("11. RUO-BASE+HRD+Virus ");
        System.out.println("12. RUO-BASE+IO+Virus ");
        System.out.println("13. RUO-BASE+methylation+Virus ");
        System.out.println("14. RUO-BASE+IO+HRD+Virus ");
        System.out.println("15. RUO-BASE+IO+methylation+Virus ");

        reportOption = userinput.nextInt();

        if (reportOption < 1 || reportOption > 10) {
            System.out.println("Enter valid option between 1-10");
            reportOption = userinput.nextInt();
        }

        switch(reportOption){
            case 1->{ReportType = "RUO_Base";}
            case 2 -> {ReportType = "RUO_Base_HRD";}
            case 3->{ReportType = "RUO_Base_methylation";}
            case 4->{ReportType = "RUO_Base_IO_HRD";}
            case 5->{ReportType = "RUO_Base_IO_methylation";}
            case 6->{ReportType = "RUO_Base_HRD_methylation";}
            case 7->{ReportType = "RUO_Base_IO_HRD_methylation";}
            case 8->{ReportType = "RUO_Base_IO";}
            case 9->{ReportType = "RUO_Base_Virus";}
            case 10->{ReportType = "RUO_Base_IO_HRD_methylation_Virus";}
            case 11->{ReportType = "RUO_BASE_HRD_Virus";}
            case 12->{ReportType = "RUO_BASE_IO_Virus";}
            case 13->{ReportType = "RUO_BASE_methylation_Virus";}
            case 14->{ReportType = "RUO_BASE_IO_HRD_Virus";}
            case 15->{ReportType = "RUO_BASE_IO_methylation_Virus";}

        }

    }
    private static  String handlingTransformationRules( int reportOption,int i,String values) {
        try {
            switch (reportOption) {
                case 1:
                case 8:
                case 9:
                case 12:
                    //  Applicable to CNV,SNV	Fusion	DenovoFusion	Indel	Deletion	AlleleType	SingleRegionMethylCall	Virus
                    //  HRD columns should be blank
                    if ((rsmd.getColumnName(i).equals("HRD_score")) || rsmd.getColumnName(i).equals("Tumor_methylation_status") || rsmd.getColumnName(i).equals("Tumor_methylation_score")) {
                        ArrayList<String> applicableVariants = new ArrayList<>(Arrays.asList("CNV",
                                "SNV",
                                "Fusion",
                                "LGR",
                                "Indel",
                                "AlleleType",
                                "PromoterMethylation",
                                "Virus"));

                        if (applicableVariants.contains(results.getString(10)) && rsmd.getColumnName(i).equals("HRD_score")) {

                            values = "";
                        }
                        if (applicableVariants.contains(results.getString(10)) && rsmd.getColumnName(i).equals("Tumor_methylation_status")) {

                            values = "";
                        }
                        ;

                        if (applicableVariants.contains(results.getString(10)) && rsmd.getColumnName(i).equals("Tumor_methylation_score")) {

                            values = "";
                        }
                }
                    break;
                case 2:
                case 4:
                case 11:
                case 14:
                    if (rsmd.getColumnName(i).equals("Tumor_methylation_status") || rsmd.getColumnName(i).equals("Tumor_methylation_score") ) {
                        ArrayList<String> applicableVariants = new ArrayList<>(Arrays.asList("CNV",
                                "SNV",
                                "Fusion",
                                "LGR",
                                "Indel",
                                "Deletion",
                                "AlleleType",
                                "PromoterMethylation",
                                "Virus"));

                       //Rule 1 :  No Tumor_methylation_status data and Tumor_methylation_score data
                        //Applicable Variants : CNV,SNV	Fusion	DenovoFusion	Indel	Deletion	AlleleType	SingleRegionMethylCall	Virus

                        if (applicableVariants.contains(results.getString(10)) && rsmd.getColumnName(i).equals("Tumor_methylation_status")) {

                            values = "";
                        }
                        ;

                        if (applicableVariants.contains(results.getString(10)) && rsmd.getColumnName(i).equals("Tumor_methylation_score")) {

                            values = "";
                        }

                    }
                    break;
                case 3:
                case 5:
                case 13:
                case 15:
                    if ((rsmd.getColumnName(i).equals("HRD_score")) ) {
                        ArrayList<String> applicableVariants = new ArrayList<>(Arrays.asList("CNV",
                                "SNV",
                                "Fusion",
                                "LGR",
                                "Indel",
                                "AlleleType",
                                "PromoterMethylation",
                                "Virus"));

                        if (applicableVariants.contains(results.getString(10)) && rsmd.getColumnName(i).equals("HRD_score")) {

                            values = "";
                        }
                    }

                case 6:
                case 7:
                case 10:

                    break;

            }
        }
            catch (Exception e) {
                e.printStackTrace();
                e.getMessage();
            }
        return values;
    }

    private static void getApplicableGeneInfo(Statement st) throws Exception {



        ArrayList<String> HRD =new ArrayList<>();
        ArrayList<String> PromoterMethylation =new ArrayList<>();
        ArrayList<String> AlleleType =new ArrayList<>();

        ArrayList<String> CNV =new ArrayList<>();
//        ArrayList<String> Deletion_base =new ArrayList<>();
        ArrayList<String> LGR =new ArrayList<>();
        ArrayList<String> Fusion =new ArrayList<>();
        ArrayList<String> Indel =new ArrayList<>();
        ArrayList<String> MSI =new ArrayList<>();
        ArrayList<String> SNV =new ArrayList<>();
        ArrayList<String> TMB =new ArrayList<>();
        ArrayList<String> SampleLevelMethylation =new ArrayList<>();
        ArrayList<String> Virus =new ArrayList<>();

        String[] reportTypeList = {"IO","base","HRD","methylation","virus"};

//        for (String repType: reportTypeList){
//            String temp = repType.replaceAll("\"", "\'");  //WHERE REPORTMODULE = '"+temp+"'"
        ResultSet reportType = st.executeQuery("SELECT DISTINCT VARIANTTYPE, LIMSTABLENAME,REPORTMODULE ,FIELDVALUES FROM LABVANTAGE.u_ghruoreportmodule  ORDER BY REPORTMODULE");





        switch (reportOption) {
            case 1: //(Only Base)
            {
                while (reportType.next()) {
                    if (reportType.getString(3).equalsIgnoreCase("base")) {
                        String variantName = reportType.getString(1);
                        {
                            switch (variantName) {
                                case "CNV" -> {
                                    if (!reportType_1.containsKey("CNV")) {

                                        reportType_1.put("CNV", CNV);

                                    }
                                    CNV.add(reportType.getString(4));

                                }

                                case "LGR" -> {
                                    if (!reportType_1.containsKey("LGR")) {

                                        reportType_1.put("LGR", LGR);
                                    }
                                    LGR.add(reportType.getString(4));
                                }
                                case "Fusion" -> {
                                    if (!reportType_1.containsKey("Fusion")) {
                                        reportType_1.put("Fusion", Fusion);
                                    }
                                    Fusion.add(reportType.getString(4));
                                }
                                case "Indel" -> {
                                    if (!reportType_1.containsKey("Indel")) {
                                        reportType_1.put("Indel", Indel);
                                    }
                                    Indel.add(reportType.getString(4));
                                }
                                case "MSI" -> {
                                    if (!reportType_1.containsKey("MSI")) {
                                        reportType_1.put("MSI", MSI);
                                    }
                                    MSI.add(reportType.getString(4));
                                }
                                case "SNV" -> {
                                    if (!reportType_1.containsKey("SNV")) {
                                        reportType_1.put("SNV", SNV);
                                    }
                                    SNV.add(reportType.getString(4));
                                }
                                case "TMB" -> {
                                    if (!reportType_1.containsKey("TMB")) {
                                        reportType_1.put("TMB", TMB);
                                    }
                                    TMB.add(reportType.getString(4));
                                }

                            }
                        }

                    }
                }
            }

            break;

            case 2: //Base +HRD
            {
                while (reportType.next()) {
                    if ((reportType.getString(3).equalsIgnoreCase("base")) ||
                            (reportType.getString(3).equalsIgnoreCase("HRD"))
                    ) {
                        {
                            String variantName = reportType.getString(1);
                            {
                                switch (variantName) {
                                    case "CNV" -> {
                                        if (!reportType_2.containsKey("CNV")) {
                                            reportType_2.put("CNV", CNV);

                                        }
                                        CNV.add(reportType.getString(4));

                                    }

                                    case "LGR" -> {
                                        if (!reportType_2.containsKey("LGR")) {
                                            reportType_2.put("LGR", LGR);
                                        }
                                        LGR.add(reportType.getString(4));
                                    }
                                    case "Fusion" -> {
                                        if (!reportType_2.containsKey("Fusion")) {
                                            reportType_2.put("Fusion", Fusion);
                                        }
                                        Fusion.add(reportType.getString(4));
                                    }
                                    case "Indel" -> {
                                        if (!reportType_2.containsKey("Indel")) {
                                            reportType_2.put("Indel", Indel);
                                        }
                                        Indel.add(reportType.getString(4));
                                    }
                                    case "MSI" -> {
                                        if (!reportType_2.containsKey("MSI")) {
                                            reportType_2.put("MSI", MSI);
                                        }
                                        MSI.add(reportType.getString(4));
                                    }
                                    case "SNV" -> {
                                        if (!reportType_2.containsKey("SNV")) {
                                            reportType_2.put("SNV", SNV);
                                        }
                                        SNV.add(reportType.getString(4));
                                    }
                                    case "TMB" -> {
                                        if (!reportType_2.containsKey("TMB")) {
                                            reportType_2.put("TMB", TMB);
                                        }
                                        TMB.add(reportType.getString(4));
                                    }
                                    case "PromoterMethylation" -> {
                                        if (!reportType_2.containsKey("PromoterMethylation")) {
                                            reportType_2.put("PromoterMethylation", PromoterMethylation);
                                        }
                                        PromoterMethylation.add(reportType.getString(4));
                                    }

                                    case "HRD" -> {
                                        if (!reportType_2.containsKey("HRD")) {
                                            reportType_2.put("HRD", HRD);
                                        }
                                        HRD.add(reportType.getString(4));
                                    }
                                }
                            }
                        }
                    }

                }
            }
            break;
            case 3: //Base +Meth
            {
                while (reportType.next()) {
                    if ((reportType.getString(3).equalsIgnoreCase("base")) ||
                            (reportType.getString(3).equalsIgnoreCase("Meth"))
                    ) {
                        {
                            String variantName = reportType.getString(1);
                            {
                                switch (variantName) {
                                    case "CNV" -> {
                                        if (!reportType_3.containsKey("CNV")) {
                                            reportType_3.put("CNV", CNV);

                                        }
                                        CNV.add(reportType.getString(4));

                                    }

                                    case "LGR" -> {
                                        if (!reportType_3.containsKey("LGR")) {
                                            reportType_3.put("LGR", LGR);
                                        }
                                        LGR.add(reportType.getString(4));
                                    }
                                    case "Fusion" -> {
                                        if (!reportType_3.containsKey("Fusion")) {
                                            reportType_3.put("Fusion", Fusion);
                                        }
                                        Fusion.add(reportType.getString(4));
                                    }
                                    case "Indel" -> {
                                        if (!reportType_3.containsKey("Indel")) {
                                            reportType_3.put("Indel", Indel);
                                        }
                                        Indel.add(reportType.getString(4));
                                    }
                                    case "MSI" -> {
                                        if (!reportType_3.containsKey("MSI")) {
                                            reportType_3.put("MSI", MSI);
                                        }
                                        MSI.add(reportType.getString(4));
                                    }
                                    case "SNV" -> {
                                        if (!reportType_3.containsKey("SNV")) {
                                            reportType_3.put("SNV", SNV);
                                        }
                                        SNV.add(reportType.getString(4));
                                    }
                                    case "TMB" -> {
                                        if (!reportType_3.containsKey("TMB")) {
                                            reportType_3.put("TMB", TMB);
                                        }
                                        TMB.add(reportType.getString(4));
                                    }
                                    case "SampleLevelMethylation" -> {
                                        if (!reportType_3.containsKey("SampleLevelMethylation")) {
                                            reportType_3.put("SampleLevelMethylation", SampleLevelMethylation);
                                        }
                                        SampleLevelMethylation.add(reportType.getString(4));
                                    }


                                }
                            }
                        }
                    }

                }
            }
            break;

            case 4: //RUO-BASE+IO+HRD
            {
                while (reportType.next()) {
                    if ((reportType.getString(3).equalsIgnoreCase("base")) ||
                            (reportType.getString(3).equalsIgnoreCase("IO")) ||
                            (reportType.getString(3).equalsIgnoreCase("HRD"))
                    ) {

                        {
                            String variantName = reportType.getString(1);
                            {
                                switch (variantName) {
                                    case "CNV" -> {
                                        if (!reportType_4.containsKey("CNV")) {
                                            reportType_4.put("CNV", CNV);

                                        }
                                        CNV.add(reportType.getString(4));

                                    }

                                    case "LGR" -> {
                                        if (!reportType_4.containsKey("LGR")) {
                                            reportType_4.put("LGR", LGR);
                                        }
                                        LGR.add(reportType.getString(4));
                                    }
                                    case "Fusion" -> {
                                        if (!reportType_4.containsKey("Fusion")) {
                                            reportType_4.put("Fusion", Fusion);
                                        }
                                        Fusion.add(reportType.getString(4));
                                    }
                                    case "Indel" -> {
                                        if (!reportType_4.containsKey("Indel")) {
                                            reportType_4.put("Indel", Indel);
                                        }
                                        Indel.add(reportType.getString(4));
                                    }
                                    case "MSI" -> {
                                        if (!reportType_4.containsKey("MSI")) {
                                            reportType_4.put("MSI", MSI);
                                        }
                                        MSI.add(reportType.getString(4));
                                    }
                                    case "SNV" -> {
                                        if (!reportType_4.containsKey("SNV")) {
                                            reportType_4.put("SNV", SNV);
                                        }
                                        SNV.add(reportType.getString(4));
                                    }
                                    case "TMB" -> {
                                        if (!reportType_4.containsKey("TMB")) {
                                            reportType_4.put("TMB", TMB);
                                        }
                                        TMB.add(reportType.getString(4));
                                    }
                                    case "PromoterMethylation" -> {
                                        if (!reportType_4.containsKey("PromoterMethylation")) {
                                            reportType_4.put("PromoterMethylation", PromoterMethylation);
                                        }
                                        PromoterMethylation.add(reportType.getString(4));
                                    }

                                    case "HRD" -> {
                                        if (!reportType_4.containsKey("HRD")) {
                                            reportType_4.put("HRD", HRD);
                                        }
                                        HRD.add(reportType.getString(4));
                                    }
                                    case "AlleleType" -> {
                                        if (!reportType_4.containsKey("AlleleType")) {
                                            reportType_4.put("AlleleType", AlleleType);
                                        }
                                        AlleleType.add(reportType.getString(4));
                                    }

                                }
                            }
                        }
                    }

                }
            }
            break;
            case 5: //RUO-BASE+IO+methylation
            {
                while (reportType.next()) {
                    if ((reportType.getString(3).equalsIgnoreCase("base")) ||
                            (reportType.getString(3).equalsIgnoreCase("IO")) ||
                            (reportType.getString(3).equalsIgnoreCase("methylation"))
                    ) {

                        {
                            String variantName = reportType.getString(1);
                            {
                                switch (variantName) {
                                    case "CNV" -> {
                                        if (!reportType_5.containsKey("CNV")) {
                                            reportType_5.put("CNV", CNV);

                                        }
                                        CNV.add(reportType.getString(4));

                                    }

                                    case "LGR" -> {
                                        if (!reportType_5.containsKey("LGR")) {
                                            reportType_5.put("LGR", LGR);
                                        }
                                        LGR.add(reportType.getString(4));
                                    }
                                    case "Fusion" -> {
                                        if (!!reportType_5.containsKey("Fusion")) {
                                            reportType_5.put("Fusion", Fusion);
                                        }
                                        Fusion.add(reportType.getString(4));
                                    }
                                    case "Indel" -> {
                                        if (!reportType_5.containsKey("Indel")) {
                                            reportType_5.put("Indel", Indel);
                                        }
                                        Indel.add(reportType.getString(4));
                                    }
                                    case "MSI" -> {
                                        if (!reportType_5.containsKey("MSI")) {
                                            reportType_5.put("MSI", MSI);
                                        }
                                        MSI.add(reportType.getString(4));
                                    }
                                    case "SNV" -> {
                                        if (!reportType_5.containsKey("SNV")) {
                                            reportType_5.put("SNV", SNV);
                                        }
                                        SNV.add(reportType.getString(4));
                                    }
                                    case "TMB" -> {
                                        if (!reportType_5.containsKey("TMB")) {
                                            reportType_5.put("TMB", TMB);
                                        }
                                        TMB.add(reportType.getString(4));
                                    }
                                    case "PromoterMethylation" -> {
                                        if (!reportType_5.containsKey("PromoterMethylation")) {
                                            reportType_5.put("PromoterMethylation", PromoterMethylation);
                                        }
                                        PromoterMethylation.add(reportType.getString(4));
                                    }

                                    case "SampleLevelMethylation" -> {
                                        if (!reportType_5.containsKey("SampleLevelMethylation")) {
                                            reportType_5.put("SampleLevelMethylation", SampleLevelMethylation);
                                        }
                                        SampleLevelMethylation.add(reportType.getString(4));
                                    }
                                    case "AlleleType" -> {
                                        if (!reportType_5.containsKey("AlleleType")) {
                                            reportType_5.put("AlleleType", AlleleType);
                                        }
                                        AlleleType.add(reportType.getString(4));
                                    }

                                }
                            }
                        }

                    }
                }
            }
            break;
            case 6: //RUO-BASE+HRD+methylation
            {
                while (reportType.next()) {
                    if ((reportType.getString(3).equalsIgnoreCase("base")) ||
                            (reportType.getString(3).equalsIgnoreCase("HRD")) ||
                            (reportType.getString(3).equalsIgnoreCase("methylation"))
                    ) {

                        {
                            String variantName = reportType.getString(1);
                            {
                                switch (variantName) {
                                    case "CNV" -> {
                                        if (!reportType_6.containsKey("CNV")) {
                                            reportType_6.put("CNV", CNV);

                                        }
                                        CNV.add(reportType.getString(4));

                                    }

                                    case "LGR" -> {
                                        if (!reportType_6.containsKey("LGR")) {
                                            reportType_6.put("LGR", LGR);
                                        }
                                        LGR.add(reportType.getString(4));
                                    }
                                    case "Fusion" -> {
                                        if (!reportType_6.containsKey("Fusion")) {
                                            reportType_6.put("Fusion", Fusion);
                                        }
                                        Fusion.add(reportType.getString(4));
                                    }
                                    case "Indel" -> {
                                        if (!reportType_6.containsKey("Indel")) {
                                            reportType_6.put("Indel", Indel);
                                        }
                                        Indel.add(reportType.getString(4));
                                    }
                                    case "MSI" -> {
                                        if (!reportType_6.containsKey("MSI")) {
                                            reportType_6.put("MSI", MSI);
                                        }
                                        MSI.add(reportType.getString(4));
                                    }
                                    case "SNV" -> {
                                        if (!reportType_6.containsKey("SNV")) {
                                            reportType_6.put("SNV", SNV);
                                        }
                                        SNV.add(reportType.getString(4));
                                    }
                                    case "TMB" -> {
                                        if (!reportType_6.containsKey("TMB")) {
                                            reportType_6.put("TMB", TMB);
                                        }
                                        TMB.add(reportType.getString(4));
                                    }
                                    case "PromoterMethylation" -> {
                                        if (!reportType_6.containsKey("PromoterMethylation")) {
                                            reportType_6.put("PromoterMethylation", PromoterMethylation);
                                        }
                                        PromoterMethylation.add(reportType.getString(4));
                                    }

                                    case "HRD" -> {
                                        if (!reportType_6.containsKey("HRD")) {
                                            reportType_6.put("HRD", HRD);
                                        }
                                        HRD.add(reportType.getString(4));
                                    }
                                    case "SampleLevelMethylation" -> {
                                        if (!reportType_6.containsKey("SampleLevelMethylation")) {
                                            reportType_6.put("SampleLevelMethylation", SampleLevelMethylation);
                                        }
                                        SampleLevelMethylation.add(reportType.getString(4));
                                    }
                                }
                            }
                        }
                    }

                }
            }
            break;

            case 7: //RUO-BASE+HRD+methylation+IO
            {
                while (reportType.next()) {
                    if ((reportType.getString(3).equalsIgnoreCase("base")) ||
                            (reportType.getString(3).equalsIgnoreCase("HRD")) ||
                            (reportType.getString(3).equalsIgnoreCase("IO")) ||
                            (reportType.getString(3).equalsIgnoreCase("methylation"))
                    ) {

                        {
                            String variantName = reportType.getString(1);
                            {
                                switch (variantName) {
                                    case "CNV" -> {
                                        if (!reportType_7.containsKey("CNV")) {
                                            reportType_7.put("CNV", CNV);

                                        }
                                        CNV.add(reportType.getString(4));

                                    }

                                    case "LGR" -> {
                                        if (!reportType_7.containsKey("LGR")) {
                                            reportType_7.put("LGR", LGR);
                                        }
                                        LGR.add(reportType.getString(4));
                                    }
                                    case "Fusion" -> {
                                        if (!reportType_7.containsKey("Fusion")) {
                                            reportType_7.put("Fusion", Fusion);
                                        }
                                        Fusion.add(reportType.getString(4));
                                    }
                                    case "Indel" -> {
                                        if (!reportType_7.containsKey("Indel")) {
                                            reportType_7.put("Indel", Indel);
                                        }
                                        Indel.add(reportType.getString(4));
                                    }
                                    case "MSI" -> {
                                        if (!reportType_7.containsKey("MSI")) {
                                            reportType_7.put("MSI", MSI);
                                        }
                                        MSI.add(reportType.getString(4));
                                    }
                                    case "SNV" -> {
                                        if (!reportType_7.containsKey("SNV")) {
                                            reportType_7.put("SNV", SNV);
                                        }
                                        SNV.add(reportType.getString(4));
                                    }
                                    case "TMB" -> {
                                        if (!reportType_7.containsKey("TMB")) {
                                            reportType_7.put("TMB", TMB);
                                        }
                                        TMB.add(reportType.getString(4));
                                    }
                                    case "PromoterMethylation" -> {
                                        if (!reportType_7.containsKey("PromoterMethylation")) {
                                            reportType_7.put("PromoterMethylation", PromoterMethylation);
                                        }
                                        PromoterMethylation.add(reportType.getString(4));
                                    }

                                    case "HRD" -> {
                                        if (!reportType_7.containsKey("HRD")) {
                                            reportType_7.put("HRD", HRD);
                                        }
                                        HRD.add(reportType.getString(4));
                                    }
                                    case "SampleLevelMethylation" -> {
                                        if (!reportType_7.containsKey("SampleLevelMethylation")) {
                                            reportType_7.put("SampleLevelMethylation", SampleLevelMethylation);
                                        }
                                        SampleLevelMethylation.add(reportType.getString(4));
                                    }
                                    case "AlleleType" -> {
                                        if (!reportType_7.containsKey("AlleleType")) {
                                            reportType_7.put("AlleleType", AlleleType);
                                        }
                                        AlleleType.add(reportType.getString(4));
                                    }
                                }
                            }
                        }
                    }

                }
            }
                break;
            case 8: //Base +IO
            {
                while (reportType.next()) {
                    if ((reportType.getString(3).equalsIgnoreCase("base")) ||
                            (reportType.getString(3).equalsIgnoreCase("IO"))

                    ) {
                        {
                            String variantName = reportType.getString(1);
                            {
                                switch (variantName) {
                                    case "CNV" -> {
                                        if (!reportType_8.containsKey("CNV")) {
                                            reportType_8.put("CNV", CNV);

                                        }
                                        CNV.add(reportType.getString(4));

                                    }

                                    case "LGR" -> {
                                        if (!reportType_8.containsKey("LGR")) {
                                            reportType_8.put("LGR", LGR);
                                        }
                                        LGR.add(reportType.getString(4));
                                    }
                                    case "Fusion" -> {
                                        if (!reportType_8.containsKey("Fusion")) {
                                            reportType_8.put("Fusion", Fusion);
                                        }
                                        Fusion.add(reportType.getString(4));
                                    }
                                    case "Indel" -> {
                                        if (!reportType_8.containsKey("Indel")) {
                                            reportType_8.put("Indel", Indel);
                                        }
                                        Indel.add(reportType.getString(4));
                                    }
                                    case "MSI" -> {
                                        if (!reportType_8.containsKey("MSI")) {
                                            reportType_8.put("MSI", MSI);
                                        }
                                        MSI.add(reportType.getString(4));
                                    }
                                    case "SNV" -> {
                                        if (!reportType_8.containsKey("SNV")) {
                                            reportType_8.put("SNV", SNV);
                                        }
                                        SNV.add(reportType.getString(4));
                                    }
                                    case "TMB" -> {
                                        if (!reportType_8.containsKey("TMB")) {
                                            reportType_8.put("TMB", TMB);
                                        }
                                        TMB.add(reportType.getString(4));
                                    }
                                    case "PromoterMethylation" -> {
                                        if (!reportType_8.containsKey("PromoterMethylation")) {
                                            reportType_8.put("PromoterMethylation", PromoterMethylation);
                                        }
                                        PromoterMethylation.add(reportType.getString(4));
                                    }


                                    case "AlleleType" -> {
                                        if (!reportType_8.containsKey("AlleleType")) {
                                            reportType_8.put("AlleleType", AlleleType);
                                        }
                                        AlleleType.add(reportType.getString(4));
                                    }


                                }
                            }
                        }
                    }

                }
            }
                break;

            case 9: //Base +Virus
            {
                while (reportType.next()) {
                    if ((reportType.getString(3).equalsIgnoreCase("base")) ||
                            (reportType.getString(3).equalsIgnoreCase("Virus"))

                    ) {
                        {
                            String variantName = reportType.getString(1);
                            {
                                switch (variantName) {
                                    case "CNV" -> {
                                        if (!reportType_9.containsKey("CNV")) {
                                            reportType_9.put("CNV", CNV);

                                        }
                                        CNV.add(reportType.getString(4));

                                    }

                                    case "LGR" -> {
                                        if (!reportType_9.containsKey("LGR")) {
                                            reportType_9.put("LGR", LGR);
                                        }
                                        LGR.add(reportType.getString(4));
                                    }
                                    case "Fusion" -> {
                                        if (!reportType_9.containsKey("Fusion")) {
                                            reportType_9.put("Fusion", Fusion);
                                        }
                                        Fusion.add(reportType.getString(4));
                                    }
                                    case "Indel" -> {
                                        if (!reportType_9.containsKey("Indel")) {
                                            reportType_9.put("Indel", Indel);
                                        }
                                        Indel.add(reportType.getString(4));
                                    }
                                    case "MSI" -> {
                                        if (!reportType_9.containsKey("MSI")) {
                                            reportType_9.put("MSI", MSI);
                                        }
                                        MSI.add(reportType.getString(4));
                                    }
                                    case "SNV" -> {
                                        if (!reportType_9.containsKey("SNV")) {
                                            reportType_9.put("SNV", SNV);
                                        }
                                        SNV.add(reportType.getString(4));
                                    }
                                    case "TMB" -> {
                                        if (!reportType_9.containsKey("TMB")) {
                                            reportType_9.put("TMB", TMB);
                                        }
                                        TMB.add(reportType.getString(4));
                                    }
                                   case "Virus" -> {
                                        if (!reportType_10.containsKey("Virus")) {
                                            reportType_10.put("Virus", Virus);
                                        }
                                        Virus.add(reportType.getString(4));
                                    }


                                }
                            }
                        }
                    }

                }
            }
            break;


            case 10: //RUO-BASE+HRD+methylation+Virus+IO
            {
                while (reportType.next()) {
                    if ((reportType.getString(3).equalsIgnoreCase("base")) ||
                            (reportType.getString(3).equalsIgnoreCase("HRD")) ||
                            (reportType.getString(3).equalsIgnoreCase("IO")) ||
                            (reportType.getString(3).equalsIgnoreCase("methylation")) ||
                            (reportType.getString(3).equalsIgnoreCase("Virus"))
                    ) {

                        {
                            String variantName = reportType.getString(1);
                            {
                                switch (variantName) {
                                    case "CNV" -> {
                                        if (!reportType_10.containsKey("CNV")) {
                                            reportType_10.put("CNV", CNV);

                                        }
                                        CNV.add(reportType.getString(4));

                                    }

                                    case "LGR" -> {
                                        if (!reportType_10.containsKey("LGR")) {
                                            reportType_10.put("LGR", LGR);
                                        }
                                        LGR.add(reportType.getString(4));
                                    }
                                    case "Fusion" -> {
                                        if (!reportType_10.containsKey("Fusion")) {
                                            reportType_10.put("Fusion", Fusion);
                                        }
                                        Fusion.add(reportType.getString(4));
                                    }
                                    case "Indel" -> {
                                        if (!reportType_10.containsKey("Indel")) {
                                            reportType_10.put("Indel", Indel);
                                        }
                                        Indel.add(reportType.getString(4));
                                    }
                                    case "MSI" -> {
                                        if (!reportType_10.containsKey("MSI")) {
                                            reportType_10.put("MSI", MSI);
                                        }
                                        MSI.add(reportType.getString(4));
                                    }
                                    case "SNV" -> {
                                        if (!reportType_10.containsKey("SNV")) {
                                            reportType_10.put("SNV", SNV);
                                        }
                                        SNV.add(reportType.getString(4));
                                    }
                                    case "TMB" -> {
                                        if (!reportType_10.containsKey("TMB")) {
                                            reportType_10.put("TMB", TMB);
                                        }
                                        TMB.add(reportType.getString(4));
                                    }
                                    case "PromoterMethylation" -> {
                                        if (!reportType_10.containsKey("PromoterMethylation")) {
                                            reportType_10.put("PromoterMethylation", PromoterMethylation);
                                        }
                                        PromoterMethylation.add(reportType.getString(4));
                                    }

                                    case "HRD" -> {
                                        if (!reportType_10.containsKey("HRD")) {
                                            reportType_10.put("HRD", HRD);
                                        }
                                        HRD.add(reportType.getString(4));
                                    }
                                    case "SampleLevelMethylation" -> {
                                        if (!reportType_10.containsKey("SampleLevelMethylation")) {
                                            reportType_10.put("SampleLevelMethylation", SampleLevelMethylation);
                                        }
                                        SampleLevelMethylation.add(reportType.getString(4));
                                    }
                                    case "AlleleType" -> {
                                        if (!reportType_10.containsKey("AlleleType")) {
                                            reportType_10.put("AlleleType", AlleleType);
                                        }
                                        AlleleType.add(reportType.getString(4));
                                    }
                                    case "Virus" -> {
                                        if (!reportType_10.containsKey("Virus")) {
                                            reportType_10.put("Virus", Virus);
                                        }
                                        Virus.add(reportType.getString(4));
                                    }
                                }
                            }
                        }
                    }

                }
            }
            case 11: // RUO-Base+Virus+HRD
            {
                while (reportType.next()) {
                    if ((reportType.getString(3).equalsIgnoreCase("base")) ||
                            (reportType.getString(3).equalsIgnoreCase("HRD")) ||
                            (reportType.getString(3).equalsIgnoreCase("Virus"))
                    ) {

                        {
                            String variantName = reportType.getString(1);
                            {
                                switch (variantName) {
                                    case "CNV" -> {
                                        if (!reportType_11.containsKey("CNV")) {
                                            reportType_11.put("CNV", CNV);

                                        }
                                        CNV.add(reportType.getString(4));

                                    }

                                    case "LGR" -> {
                                        if (!reportType_11.containsKey("LGR")) {
                                            reportType_11.put("LGR", LGR);
                                        }
                                        LGR.add(reportType.getString(4));
                                    }
                                    case "Fusion" -> {
                                        if (!reportType_11.containsKey("Fusion")) {
                                            reportType_11.put("Fusion", Fusion);
                                        }
                                        Fusion.add(reportType.getString(4));
                                    }
                                    case "Indel" -> {
                                        if (!reportType_11.containsKey("Indel")) {
                                            reportType_11.put("Indel", Indel);
                                        }
                                        Indel.add(reportType.getString(4));
                                    }
                                    case "MSI" -> {
                                        if (!reportType_11.containsKey("MSI")) {
                                            reportType_11.put("MSI", MSI);
                                        }
                                        MSI.add(reportType.getString(4));
                                    }
                                    case "SNV" -> {
                                        if (!reportType_11.containsKey("SNV")) {
                                            reportType_11.put("SNV", SNV);
                                        }
                                        SNV.add(reportType.getString(4));
                                    }
                                    case "TMB" -> {
                                        if (!reportType_11.containsKey("TMB")) {
                                            reportType_11.put("TMB", TMB);
                                        }
                                        TMB.add(reportType.getString(4));
                                    }
                                    case "PromoterMethylation" -> {
                                        if (!reportType_11.containsKey("PromoterMethylation")) {
                                            reportType_11.put("PromoterMethylation", PromoterMethylation);
                                        }
                                        PromoterMethylation.add(reportType.getString(4));
                                    }

                                    case "HRD" -> {
                                        if (!reportType_11.containsKey("HRD")) {
                                            reportType_11.put("HRD", HRD);
                                        }
                                        HRD.add(reportType.getString(4));
                                    }

                                    case "Virus" -> {
                                        if (!reportType_11.containsKey("Virus")) {
                                            reportType_11.put("Virus", Virus);
                                        }
                                        Virus.add(reportType.getString(4));
                                    }
                                }
                            }
                        }
                    }

                }
//
            }
            break;

            case 12:// RUO-Base+Virus+IO
            {
                while (reportType.next()) {
                    if ((reportType.getString(3).equalsIgnoreCase("base")) ||
                            (reportType.getString(3).equalsIgnoreCase("IO")) ||
                            (reportType.getString(3).equalsIgnoreCase("Virus"))
                    ) {

                        {
                            String variantName = reportType.getString(1);
                            {
                                switch (variantName) {
                                    case "CNV" -> {
                                        if (!reportType_12.containsKey("CNV")) {
                                            reportType_12.put("CNV", CNV);

                                        }
                                        CNV.add(reportType.getString(4));

                                    }

                                    case "LGR" -> {
                                        if (!reportType_12.containsKey("LGR")) {
                                            reportType_12.put("LGR", LGR);
                                        }
                                        LGR.add(reportType.getString(4));
                                    }
                                    case "Fusion" -> {
                                        if (!reportType_12.containsKey("Fusion")) {
                                            reportType_12.put("Fusion", Fusion);
                                        }
                                        Fusion.add(reportType.getString(4));
                                    }
                                    case "Indel" -> {
                                        if (!reportType_12.containsKey("Indel")) {
                                            reportType_12.put("Indel", Indel);
                                        }
                                        Indel.add(reportType.getString(4));
                                    }
                                    case "MSI" -> {
                                        if (!reportType_12.containsKey("MSI")) {
                                            reportType_12.put("MSI", MSI);
                                        }
                                        MSI.add(reportType.getString(4));
                                    }
                                    case "SNV" -> {
                                        if (!reportType_12.containsKey("SNV")) {
                                            reportType_12.put("SNV", SNV);
                                        }
                                        SNV.add(reportType.getString(4));
                                    }
                                    case "TMB" -> {
                                        if (!reportType_12.containsKey("TMB")) {
                                            reportType_12.put("TMB", TMB);
                                        }
                                        TMB.add(reportType.getString(4));
                                    }
                                    case "PromoterMethylation" -> {
                                        if (!reportType_12.containsKey("PromoterMethylation")) {
                                            reportType_12.put("PromoterMethylation", PromoterMethylation);
                                        }
                                        PromoterMethylation.add(reportType.getString(4));
                                    }

//                                case "HRD" -> {
//                                    if (!reportType_12.containsKey("HRD")) {
//                                        reportType_12.put("HRD", HRD);
//                                    }
//                                    HRD.add(reportType.getString(4));
//                                }

                                    case "AlleleType" -> {
                                        if (!reportType_12.containsKey("AlleleType")) {
                                            reportType_12.put("AlleleType", AlleleType);
                                        }
                                        AlleleType.add(reportType.getString(4));
                                    }
                                    case "Virus" -> {
                                        if (!reportType_12.containsKey("Virus")) {
                                            reportType_12.put("Virus", Virus);
                                        }
                                        Virus.add(reportType.getString(4));
                                    }
                                }
                            }
                        }
                    }

                }


            }break;


            case 13:// RUO-Base+Virus+Methylation
            {
                while (reportType.next()) {
                    if ((reportType.getString(3).equalsIgnoreCase("base")) ||
                            (reportType.getString(3).equalsIgnoreCase("methylation")) ||
                            (reportType.getString(3).equalsIgnoreCase("Virus"))
                    ) {

                        {
                            String variantName = reportType.getString(1);
                            {
                                switch (variantName) {
                                    case "CNV" -> {
                                        if (!reportType_13.containsKey("CNV")) {
                                            reportType_13.put("CNV", CNV);

                                        }
                                        CNV.add(reportType.getString(4));

                                    }

                                    case "LGR" -> {
                                        if (!reportType_13.containsKey("LGR")) {
                                            reportType_13.put("LGR", LGR);
                                        }
                                        LGR.add(reportType.getString(4));
                                    }
                                    case "Fusion" -> {
                                        if (!reportType_13.containsKey("Fusion")) {
                                            reportType_13.put("Fusion", Fusion);
                                        }
                                        Fusion.add(reportType.getString(4));
                                    }
                                    case "Indel" -> {
                                        if (!reportType_13.containsKey("Indel")) {
                                            reportType_13.put("Indel", Indel);
                                        }
                                        Indel.add(reportType.getString(4));
                                    }
                                    case "MSI" -> {
                                        if (!reportType_13.containsKey("MSI")) {
                                            reportType_13.put("MSI", MSI);
                                        }
                                        MSI.add(reportType.getString(4));
                                    }
                                    case "SNV" -> {
                                        if (!reportType_13.containsKey("SNV")) {
                                            reportType_13.put("SNV", SNV);
                                        }
                                        SNV.add(reportType.getString(4));
                                    }
                                    case "TMB" -> {
                                        if (!reportType_13.containsKey("TMB")) {
                                            reportType_13.put("TMB", TMB);
                                        }
                                        TMB.add(reportType.getString(4));
                                    }

                                    case "SampleLevelMethylation" -> {
                                        if (!reportType_13.containsKey("SampleLevelMethylation")) {
                                            reportType_13.put("SampleLevelMethylation", SampleLevelMethylation);
                                        }
                                        SampleLevelMethylation.add(reportType.getString(4));
                                    }

                                    case "Virus" -> {
                                        if (!reportType_13.containsKey("Virus")) {
                                            reportType_13.put("Virus", Virus);
                                        }
                                        Virus.add(reportType.getString(4));
                                    }
                                }
                            }
                        }
                    }

                }

            }break;

//                IO->Allele,PromoterMethyl,CNV,
//                Virus -> Virus
//                Base->LGR,SNV,TMB,Fusion,CNV, Indel, MSI
//                HRD->PromoterMethylation,HRD
//                methylation-> SampleLevelMethylation


            case 14: //Base+IO+HRD+virus
            {
                while (reportType.next()) {
                    if ((reportType.getString(3).equalsIgnoreCase("base")) ||
                            (reportType.getString(3).equalsIgnoreCase("HRD")) ||
                            (reportType.getString(3).equalsIgnoreCase("IO")) ||

                            (reportType.getString(3).equalsIgnoreCase("Virus"))
                    ) {

                        {
                            String variantName = reportType.getString(1);
                            {
                                switch (variantName) {
                                    case "CNV" -> {
                                        if (!reportType_14.containsKey("CNV")) {
                                            reportType_14.put("CNV", CNV);

                                        }
                                        CNV.add(reportType.getString(4));

                                    }

                                    case "LGR" -> {
                                        if (!reportType_14.containsKey("LGR")) {
                                            reportType_14.put("LGR", LGR);
                                        }
                                        LGR.add(reportType.getString(4));
                                    }
                                    case "Fusion" -> {
                                        if (!reportType_14.containsKey("Fusion")) {
                                            reportType_14.put("Fusion", Fusion);
                                        }
                                        Fusion.add(reportType.getString(4));
                                    }
                                    case "Indel" -> {
                                        if (!reportType_14.containsKey("Indel")) {
                                            reportType_14.put("Indel", Indel);
                                        }
                                        Indel.add(reportType.getString(4));
                                    }
                                    case "MSI" -> {
                                        if (!reportType_14.containsKey("MSI")) {
                                            reportType_14.put("MSI", MSI);
                                        }
                                        MSI.add(reportType.getString(4));
                                    }
                                    case "SNV" -> {
                                        if (!reportType_14.containsKey("SNV")) {
                                            reportType_14.put("SNV", SNV);
                                        }
                                        SNV.add(reportType.getString(4));
                                    }
                                    case "TMB" -> {
                                        if (!reportType_14.containsKey("TMB")) {
                                            reportType_14.put("TMB", TMB);
                                        }
                                        TMB.add(reportType.getString(4));
                                    }
                                    case "PromoterMethylation" -> {
                                        if (!reportType_14.containsKey("PromoterMethylation")) {
                                            reportType_14.put("PromoterMethylation", PromoterMethylation);
                                        }
                                        PromoterMethylation.add(reportType.getString(4));
                                    }

                                    case "HRD" -> {
                                        if (!reportType_14.containsKey("HRD")) {
                                            reportType_14.put("HRD", HRD);
                                        }
                                        HRD.add(reportType.getString(4));
                                    }

                                    case "AlleleType" -> {
                                        if (!reportType_14.containsKey("AlleleType")) {
                                            reportType_14.put("AlleleType", AlleleType);
                                        }
                                        AlleleType.add(reportType.getString(4));
                                    }
                                    case "Virus" -> {
                                        if (!reportType_14.containsKey("Virus")) {
                                            reportType_14.put("Virus", Virus);
                                        }
                                        Virus.add(reportType.getString(4));
                                    }
                                }
                            }
                        }
                    }

                }
            }
            break;

            case 15 : //Base+IO+ Meth +virus
            {
                while (reportType.next()) {
                    if ((reportType.getString(3).equalsIgnoreCase("base")) ||
                            (reportType.getString(3).equalsIgnoreCase("IO")) ||
                            (reportType.getString(3).equalsIgnoreCase("methylation")) ||
                            (reportType.getString(3).equalsIgnoreCase("Virus"))
                    ) {

                        {
                            String variantName = reportType.getString(1);
                            {
                                switch (variantName) {
                                    case "CNV" -> {
                                        if (!reportType_15.containsKey("CNV")) {
                                            reportType_15.put("CNV", CNV);

                                        }
                                        CNV.add(reportType.getString(4));

                                    }

                                    case "LGR" -> {
                                        if (!reportType_15.containsKey("LGR")) {
                                            reportType_15.put("LGR", LGR);
                                        }
                                        LGR.add(reportType.getString(4));
                                    }
                                    case "Fusion" -> {
                                        if (!reportType_15.containsKey("Fusion")) {
                                            reportType_15.put("Fusion", Fusion);
                                        }
                                        Fusion.add(reportType.getString(4));
                                    }
                                    case "Indel" -> {
                                        if (!reportType_15.containsKey("Indel")) {
                                            reportType_15.put("Indel", Indel);
                                        }
                                        Indel.add(reportType.getString(4));
                                    }
                                    case "MSI" -> {
                                        if (!reportType_15.containsKey("MSI")) {
                                            reportType_15.put("MSI", MSI);
                                        }
                                        MSI.add(reportType.getString(4));
                                    }
                                    case "SNV" -> {
                                        if (!reportType_15.containsKey("SNV")) {
                                            reportType_15.put("SNV", SNV);
                                        }
                                        SNV.add(reportType.getString(4));
                                    }
                                    case "TMB" -> {
                                        if (!reportType_15.containsKey("TMB")) {
                                            reportType_15.put("TMB", TMB);
                                        }
                                        TMB.add(reportType.getString(4));
                                    }
                                    case "PromoterMethylation" -> {
                                        if (!reportType_15.containsKey("PromoterMethylation")) {
                                            reportType_15.put("PromoterMethylation", PromoterMethylation);
                                        }
                                        PromoterMethylation.add(reportType.getString(4));
                                    }


                                    case "SampleLevelMethylation" -> {
                                        if (!reportType_15.containsKey("SampleLevelMethylation")) {
                                            reportType_15.put("SampleLevelMethylation", SampleLevelMethylation);
                                        }
                                        SampleLevelMethylation.add(reportType.getString(4));
                                    }
                                    case "AlleleType" -> {
                                        if (!reportType_15.containsKey("AlleleType")) {
                                            reportType_15.put("AlleleType", AlleleType);
                                        }
                                        AlleleType.add(reportType.getString(4));
                                    }
                                    case "Virus" -> {
                                        if (!reportType_15.containsKey("Virus")) {
                                            reportType_15.put("Virus", Virus);
                                        }
                                        Virus.add(reportType.getString(4));
                                    }
                                }
                            }
                        }
                    }

                }
            }
            break;

        }


    }

//    public static void compareRUOBatchReportWithCumulativeReport(String reportLocation,String SampleIDInput){
//
//        System.out.println("Do you want to compare RUO Report with Cumulative Report for the given SampleID");
//        System.out.println("1. Yes");
//        System.out.println("2. No");
//        Scanner userinput= new Scanner(System.in);
//        int Option = userinput.nextInt();
//        try {
//        switch(Option){
//            case 2:
//                break;
//
//            case 1:
//                System.out.println(" Enter the location of the Cumulative Report location");
//                String reportlocation =  userinput.nextLine();
//                CumulativeReportHandlers creport = new CumulativeReportHandlers();
//                HashMap<String, HashMap<String, ArrayList<Map>>> readCumulativeReport = creport.readCumulativeReport(reportlocation,SampleIDInput);
//
//                String[] sampleIDlst;
//                String tmp;
//
//                if (SampleIDInput.contains(",")){
//                    tmp = String.join(",",SampleIDInput.trim().split("\\s*,\\s*"));
//
//                }
//                else{
//                    tmp= SampleIDInput.trim();
//                }
//                sampleIDlst = tmp.split(",");
//
//                for (String singleSampleID: sampleIDlst){
//
//                }
//
//                break;
//        }
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//
//
////        while
//
//    }




}



