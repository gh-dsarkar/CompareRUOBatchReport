import org.junit.Assert;

import java.sql.*;
import java.util.HashMap;

public class ghDbVsLIMSCheck {
//    public static
    private static ResultSetMetaData rsmd;
    private static ResultSet results;

    private static ResultSet GHDBresult;

    public static HashMap<String,String> LIMSDataMap = new HashMap<>();
    public static HashMap<String,String> GHDBDataMap = new HashMap<>();




    public void getLIMSdata(){
        String username;
        String password ;
        String hostname ;
        String port ;
        String SID ;

        String DB_LIMS_Schema;
        String DB_ServiceName;
        String connectionString ;
        Connection con;




            username = "dsarkar";
            password = "r2x12Xb(Pf902ieAn9r)KB00Y";
            hostname = "gh-sqa-db-lims.ghdna.io";
            port = "1521";
            SID = "limssqa";
            DB_LIMS_Schema = "labvantage";
            DB_ServiceName = "ocrl";
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

                con.setSchema(DB_LIMS_Schema);
                System.out.println("Connected to Database succesfully");


                //Read records from db

                Statement st = con.createStatement(
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY
                );

                st.executeQuery("ALTER SESSION SET CURRENT_SCHEMA = labvantage");
                results = st.executeQuery("SQL Query");


                    rsmd = results.getMetaData();
                    int dbColumns = rsmd.getColumnCount();
                    for (int i = 1; i < dbColumns + 1; i++) {
                        String values = results.getString(i);
                        LIMSDataMap.put(rsmd.getColumnName(i).toString(), values);
                        }


            }
            catch (Exception e) {
                e.getMessage();
            }

        }

    public void getGhDBdata(){

        String username = "admin";
        String password = "N7Tks0xPS";
        String JDBCUrl = "jdbc:postgresql://10.4.170.107/ghdb";
        Connection con = null;
        Statement st;


        try {
            Class.forName("org.postgresql.Driver");

        } catch (ClassNotFoundException e) {
//            //
            e.printStackTrace();
        }

        try {

            con = DriverManager.getConnection(JDBCUrl, username, password);
            st = con.createStatement();
            System.out.println("INFO: GHDB connection established");

            String SNVQuery = "Select * from snv_call where run_sample_id = "+"B00151173"+" and runid ="+"runid"+"";

            ResultSet rs = st.executeQuery(SNVQuery);


                rsmd = rs.getMetaData();
                int dbColumns = rsmd.getColumnCount();
                String GHDBresult = rs.getString(1);

                for (int i = 1; i < dbColumns + 1; i++) {
                    String values = rs.getString(i);
                    GHDBDataMap.put(rsmd.getColumnName(i).toString(), values);
                }

        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            if (con == null) {

                throw new Exception("ERROR: Connection cannot be established.");
            }
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }

    }


    public void compareSNVData(HashMap <String,String> GHDBDataMap , HashMap <String,String> LIMSDataMap){
        Assert.assertEquals(GHDBDataMap.get("TRANSCRIPT_ID"),LIMSDataMap.get("TRANSCRIPT_ID"));
        Assert.assertEquals(GHDBDataMap.get("CDNA"),LIMSDataMap.get("CDNA"));
        Assert.assertEquals(GHDBDataMap.get("SPLICE_EFFECT"),LIMSDataMap.get("SPLICEEFFECT"));
        Assert.assertEquals(GHDBDataMap.get("CALL"),LIMSDataMap.get("SNVCALL"));
        Assert.assertEquals(GHDBDataMap.get("ANALYSIS_VERSION"),LIMSDataMap.get("ANALYSISVERSION"));
        Assert.assertEquals(GHDBDataMap.get("TB_CODE"),LIMSDataMap.get("TBCODE"));

        String comment = GHDBDataMap.get("VARIANT_COMMENT")==null ? "":GHDBDataMap.get("VARIANT_COMMENT").replaceAll(";",",");
        Assert.assertEquals(comment,LIMSDataMap.get("VARIANTCOMMENT"));
        Assert.assertEquals(GHDBDataMap.get("MOL_CNT"),LIMSDataMap.get("MOL_CNT"));
        Assert.assertEquals(GHDBDataMap.get("REPORTING_CATEGORY"),LIMSDataMap.get("REPORTING_CATEGORY"));
        Assert.assertEquals(GHDBDataMap.get("EXON"),LIMSDataMap.get("EXON"));
        Assert.assertEquals(GHDBDataMap.get("SOMATIC_CALL"),LIMSDataMap.get("SOMATIC_CALL"));
        Assert.assertEquals(GHDBDataMap.get("SOMATIC_REVIEW"),LIMSDataMap.get("SOMATIC_REVIEW"));
        Assert.assertEquals(GHDBDataMap.get("SOMATIC_SCORE"),LIMSDataMap.get("SOMATIC_SCORE"));
        Assert.assertEquals(GHDBDataMap.get("SOMATIC_CALL"),LIMSDataMap.get("LIMS_SOMATIC_CALL"));
        Assert.assertEquals(GHDBDataMap.get("TOMOR_CALL"),LIMSDataMap.get("TUMORCALL"));
        Assert.assertEquals(GHDBDataMap.get("LDT_REPORTABLE"),LIMSDataMap.get("LDTREPORTABLE"));
        Assert.assertEquals(GHDBDataMap.get("CLINVAR_CLINSIG"),LIMSDataMap.get("CLINVAR_CLINSIG"));
        Assert.assertEquals(GHDBDataMap.get("MOLECULAR_CONSEQUENCE"),LIMSDataMap.get("MOLECULAR_CONSEQUENCE"));
        Assert.assertEquals(GHDBDataMap.get("FUNCTIONAL_IMPACT"),LIMSDataMap.get("FUNCTIONAL_IMPACT"));
        Assert.assertEquals(GHDBDataMap.get("MUTANT_ALLELE_STATUS"),LIMSDataMap.get("MUTANT_ALLELE_STATUS"));
        Assert.assertEquals(GHDBDataMap.get("CLINVAR_ID"),LIMSDataMap.get("CLINVAR_ID"));
        Assert.assertEquals(GHDBDataMap.get("GRS_VAR+NDT_SCORE"),LIMSDataMap.get("GRS_SCORE"));
        Assert.assertEquals(GHDBDataMap.get("GRS_USED"),LIMSDataMap.get("GRS_USED"));
        Assert.assertEquals(GHDBDataMap.get("TVF_CALL_MULTITUMOR"),LIMSDataMap.get("TVF_CALLED"));
        Assert.assertEquals(GHDBDataMap.get("RUO_REPORTABLE"),LIMSDataMap.get("RUOREPORTABLE"));

    }


}
