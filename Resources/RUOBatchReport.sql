WITH SAMPLE_LIST AS (
                        SELECT 'B00148978' as "SAMPLE_ID"
                           FROM dual d
                        ),
            ACTIVESAMPLES AS (
                        SELECT  v."SampleID", v."RunID"
                            FROM SAMPLE_LIST sl
                            INNER JOIN V_RESEARCH_REQUEST_INFO v ON sl."SAMPLE_ID" = v."SampleID" AND v."RecordStatus" = 'Activating'
            ),
            RUOREPORTMODULES AS (
                SELECT DISTINCT ruorm.LIMSTABLENAME, ruorm.FIELDNAMES, ruorm.FIELDVALUES, ruorm.VARIANTTYPE
                FROM u_ghruoreportmodule ruorm
                WHERE ruorm.reportmoduleversion = 'v1.1.1'
                AND (exists (select ruoreportmodule from u_ruomoduleassignment where s_projectid = 'GHI_03_Research'
                and ruorm.reportmodule = ruoreportmodule) OR 'base' = ruorm.reportmodule)
                order by LIMSTABLENAME, FIELDVALUES
            ),
            SNV AS (
                   SELECT 'U_GHSNV' as LIMSTABLENAME,
                        RUOREPORTMODULES.VARIANTTYPE as "Variant_type",
                        sampleid as "SAMPLE_ID",
                        runid as "RUN_ID",
                        gene as "Gene",
                        chromosome as "Chromosome",
                        position as "Position",
                        CONCAT(CONCAT('=',exon), '') as "Exon",
--                        to_char(EXON) as "Exon",
                        (CASE WHEN UPPER(SOMATIC_CALL) = UPPER('somatic') THEN 1 ELSE 0 END) as "Alt-NoAlt",
                        mutation_nt as "Mut_nt",
                        mutation_aa as "Mut_aa",
                        spliceeffect as "Splice_effect",
                        cdna as "Mut_cdna",
                        transcript_id as "Transcript",
                        TO_CHAR(percentage, 'fm990D00') as "Percentage",
                        somatic_call as "Somatic_status",
                        molecular_consequence as "Molecular_consequence",
                        NULL as "CNV_type",
                        cosmic_id as "COSMIC",
                        dbsnp_id as "dbSNP",
                        clinvar_clinsig as "ClinVar",
                        clinvar_id as "ClinVarID",
                        functional_impact as "Functional_impact",
                        mutant_allele_status  as "Mutant_allele_status",
                        NULL as "Fusion_chrom_b",
                        NULL as "Fusion_gene_b",
                        NULL as  "Fusion_position_a",
                        NULL as "Fusion_position_b",
                        NULL as "Direction_a",
                        NULL as "Direction_b",
                        NULL as "Downstream_gene",
                        NULL as "Copy_number",
                        mol_cnt as "Mol_count",
                        NULL as "Indel_type",
                        NULL as "Alleletype"
                FROM u_ghsnv
                INNER JOIN RUOREPORTMODULES ON LIMSTABLENAME = 'U_GHSNV'
                WHERE ISPOSITIVEBYREVIEW = '1' and RUOREPORTABLE = 1 and exists (SELECT SampleID from ACTIVESAMPLES where u_ghsnv.sampleid = ACTIVESAMPLES."SampleID" and u_ghsnv.runid = ACTIVESAMPLES."RunID")
            ),
            CNV AS (
                SELECT 'U_GHCNVGENE' as LIMSTABLENAME,
                        RUOREPORTMODULES.VARIANTTYPE as "Variant_type",
                        sampleid as "SAMPLE_ID",
                        runid as "RUN_ID",
                        gene as "Gene",
                        chromosome as "Chromosome",
                        NULL AS "Position",
                        NULL as "Exon",
                        1 as "Alt-NoAlt",
                        NULL as "Mut_nt",
                        NULL as "Mut_aa",
                        NULL as "Splice_effect",
                        NULL as "Mut_cdna",
                        NULL as "Transcript",
                        NULL as "Percentage",
                        'somatic' as "Somatic_status",
                        NULL as "Molecular_consequence",
                        (CASE WHEN (CNVCALL = 1) THEN 'amplification'
                             WHEN (CNVCALL = 2) THEN 'focal_amplification'
                             WHEN (CNVCALL = 3) THEN 'aneuploid_amplification'END) as "CNV_type",
                        NULL as "COSMIC",
                        NULL as "dbSNP",
                        NULL as "ClinVar",
                        NULL as "ClinVarID",
                        NULL as "Functional_impact",
                        NULL as "Mutant_allele_status",
                        NULL as "Fusion_chrom_b",
                        NULL as "Fusion_gene_b",
                        NULL as  "Fusion_position_a",
                        NULL as "Fusion_position_b",
                        NULL as "Direction_a",
                        NULL as "Direction_b",
                        NULL as "Downstream_gene",
                        copynumber as "Copy_number",
                        NULL as "Mol_count",
                        NULL as "Indel_type",
                        NULL as "Alleletype"
                FROM u_ghcnvgene
                INNER JOIN RUOREPORTMODULES ON LIMSTABLENAME = 'U_GHCNVGENE'
                WHERE ISPOSITIVEBYREVIEW = '1' and RUOREPORTABLE = 1 and exists (SELECT SampleID from ACTIVESAMPLES where u_ghcnvgene.sampleid = ACTIVESAMPLES."SampleID" and u_ghcnvgene.runid = ACTIVESAMPLES."RunID")
            ),
            DELETION AS (
               SELECT   'U_GHDELETION' as LIMSTABLENAME,
                        RUOREPORTMODULES.VARIANTTYPE as "Variant_type",
                        sampleid as "SAMPLE_ID",
                        runid as "RUN_ID",
                        gene as "Gene",
                        chrom as "Chromosome",
                        NULL as "Position",
                        NULL as "Exon",
                        0 as "Alt-NoAlt",
                        NULL as "Mut_nt",
                        NULL as "Mut_aa",
                        NULL as "Splice_effect",
                        NULL as "Mut_cdna",
                        NULL as "Transcript",
                        NULL as "Percentage",
                        'somatic' as "Somatic_status",
                        NULL as "Molecular_consequence",
                        cnv_type as "CNV_type",
                        NULL as "COSMIC",
                        NULL as "dbSNP",
                        NULL as "ClinVar",
                        NULL as "ClinVarID",
                        NULL as "Functional_impact",
                        MUTANT_ALLELE_STATUS as "Mutant_allele_status",
                        NULL as "Fusion_chrom_b",
                        NULL as "Fusion_gene_b",
                        NULL as  "Fusion_position_a",
                        NULL as "Fusion_position_b",
                        NULL as "Direction_a",
                        NULL as "Direction_b",
                        NULL as "Downstream_gene",
                        ROUND(COPY_NUMBER, 2) as "Copy_number",
                        NULL as "Mol_count",
                        NULL as "Indel_type",
                        NULL as "Alleletype"
                FROM u_ghdeletion del
                INNER JOIN RUOREPORTMODULES ON LIMSTABLENAME = 'U_GHDELETION' AND
                   FIELDNAMES = 'gene' AND FIELDVALUES = del.gene
                WHERE del.ISPOSITIVEBYREVIEW = '1' and del.RUOREPORTABLE = 1 and exists (SELECT SampleID from ACTIVESAMPLES where del.sampleid = ACTIVESAMPLES."SampleID" and del.runid = ACTIVESAMPLES."RunID")
            ),
            INDEL AS (
                SELECT  'U_GHINDEL' as LIMSTABLENAME,
                        RUOREPORTMODULES.VARIANTTYPE as "Variant_type",
                        sampleid AS "SAMPLE_ID",
                        runid AS "RUN_ID",
                        gene as "Gene",
                        chromosome as "Chromosome",
                        position_start as "Position",
--                        CASE WHEN exon > 0 then TO_CHAR(exon) END as "Exon",
                        CASE WHEN exon > 0 then CONCAT(CONCAT('=',exon), '') END as "Exon",

                        (CASE WHEN UPPER(SOMATIC_CALL) = UPPER('somatic') THEN 1 ELSE 0 END) as "Alt-NoAlt",
                        mut_nt as "Mut_nt",
                        aminoacid_mutation as "Mut_aa",
                        spliceeffect as "Splice_effect",
                        cdna as "Mut_cdna",
                        transcript_id as "Transcript",
                        TO_CHAR(percentage, 'fm990D00') as "Percentage",
                        lims_somatic_call as "Somatic_status",
                        molecular_consequence as "Molecular_consequence",
                        NULL as "CNV_type",
                        cosmic_id as "COSMIC",
                        dbsnp_id as "dbSNP",
                        clinvar_clinsig as "ClinVar",
                        clinvar_id as "ClinVarID",
                        functional_impact as "Functional_impact",
                        mutant_allele_status as "Mutant_allele_status",
                        NULL as "Fusion_chrom_b",
                        NULL as "Fusion_gene_b",
                        NULL as "Fusion_position_a",
                        NULL as "Fusion_position_b",
                        NULL as "Direction_a",
                        NULL as "Direction_b",
                        NULL as "Downstream_gene",
                        NULL as "Copy_number",
                        fam_cnt as "Mol_count",
                        type as "Indel_type",
                        NULL as "Alleletype"
                FROM u_ghindel
                INNER JOIN RUOREPORTMODULES ON LIMSTABLENAME = 'U_GHINDEL'
                WHERE ISPOSITIVEBYREVIEW = '1' and RUOREPORTABLE = 1 and exists (SELECT SampleID from ACTIVESAMPLES where u_ghindel.sampleid = ACTIVESAMPLES."SampleID" and u_ghindel.runid = ACTIVESAMPLES."RunID")
            ),

            FUSION AS (
                SELECT  'U_GHFUSION' as LIMSTABLENAME,
                        RUOREPORTMODULES.VARIANTTYPE as "Variant_type",
                        sampleid AS "SAMPLE_ID",
                        runid AS "RUN_ID",
                        gene_a as "Gene",
                        chromosome_a as "Chromosome",
                        NULL as "Position",
                        NULL as "Exon",
                        1 as "Alt-NoAlt",
                        NULL as "Mut_nt",
                        NULL as "Mut_aa",
                        NULL as "Splice_effect",
                        NULL as "Mut_cdna",
                        NULL as "Transcript",
                        TO_CHAR(percent_fusion_ab, 'fm990D00') as "Percentage",
                        'somatic' as "Somatic_status",
                        NULL as "Molecular_consequence",
                        NULL as "CNV_type",
                        NULL as "COSMIC",
                        NULL as "dbSNP",
                        NULL as "ClinVar",
                        NULL as "ClinVarID",
                        NULL as "Functional_impact",
                        NULL as "Mutant_allele_status",
                        chromosome_b as "Fusion_chrom_b",
                        gene_b as "Fusion_gene_b",
                        position_a as  "Fusion_position_a",
                        position_b as "Fusion_position_b",
                        direction_a as "Direction_a",
                       direction_b as "Direction_b",
                        downstream_gene as "Downstream_gene",
                        NULL as "Copy_number",
                        greatest(wildtype_molecule_count_a, wildtype_molecule_count_b) as "Mol_count",
                        NULL as "Indel_type",
                        NULL as "Alleletype"
                FROM u_ghfusion
                INNER JOIN RUOREPORTMODULES ON LIMSTABLENAME = 'U_GHFUSION'
                WHERE ISPOSITIVEBYREVIEW = '1' and RUOREPORTABLE = 1 and exists (SELECT SampleID from ACTIVESAMPLES where u_ghfusion.sampleid = ACTIVESAMPLES."SampleID" and u_ghfusion.runid = ACTIVESAMPLES."RunID")
            ),

            DENOVOFUSION AS (
                SELECT 'U_GHDENOVOFUSION' as LIMSTABLENAME,
                        RUOREPORTMODULES.VARIANTTYPE as "Variant_type",
                        sampleid as "SAMPLE_ID",
                        runid as "RUN_ID",
                        gene_a as "Gene",
                        chrom_a as "Chromosome",
                        pos_a as "Position",
                        to_char(EXONS) as "Exon",
                        (CASE WHEN UPPER(SOMATIC_CALL) = UPPER('somatic') THEN 1 ELSE 0 END) as "Alt-NoAlt",
                        NULL as "Mut_nt",
                        NULL as "Mut_aa",
                        NULL as "Splice_effect",
                        NULL as "Mut_cdna",
                        NULL as "Transcript",
                        TO_CHAR(percentage, 'fm990D00') as "Percentage",
                        somatic_call as "Somatic_status",
                        NULL as "Molecular_consequence",
                        NULL as "CNV_type",
                        NULL as "COSMIC",
                        NULL as "dbSNP",
                        NULL as "ClinVar",
                        NULL as "ClinVarID",
                        functional_impact as "Functional_impact",
                        mutant_allele_status as "Mutant_allele_status",
                        chrom_b as "Fusion_chrom_b",
                        gene_b as "Fusion_gene_b",
                        pos_a as "Fusion_position_a",
                        pos_b as "Fusion_position_b",
                        direction_a as "Direction_a",
                        direction_b as "Direction_b",
                        downstream_gene as "Downstream_gene",
                        NULL as "Copy_number",
                        mol_cnt as "Mol_count",
                        NULL as "Indel_type",
                        NULL as "Alleletype"
                FROM u_ghdenovofusion
                INNER JOIN RUOREPORTMODULES ON LIMSTABLENAME = 'U_GHDENOVOFUSION'
                WHERE ISPOSITIVEBYREVIEW = '1' and RUOREPORTABLE = 1 and exists (SELECT SampleID from ACTIVESAMPLES where u_ghdenovofusion.sampleid = ACTIVESAMPLES."SampleID" and u_ghdenovofusion.runid = ACTIVESAMPLES."RunID")
            ),
            SINGLEREGIONMETHYL AS (
                SELECT 'U_GHSINGLEMETHYL' as LIMSTABLENAME,
                        RUOREPORTMODULES.VARIANTTYPE as "Variant_type",
                        sampleid as "SAMPLE_ID",
                        runid as "RUN_ID",
                        gene as "Gene",
                        NULL as "Chromosome",
                        NULL as "Position",
                        NULL as "Exon",
                        0 as "Alt-NoAlt",
                        NULL as "Mut_nt",
                        NULL as "Mut_aa",
                        NULL as "Splice_effect",
                        NULL as "Mut_cdna",
                        NULL as "Transcript",
                        NULL as "Percentage",
                        NULL as "Somatic_status",
                        NULL as "Molecular_consequence",
                        NULL as "CNV_type",
                        NULL as "COSMIC",
                        NULL as "dbSNP",
                        NULL as "ClinVar",
                        NULL as "ClinVarID",
                        NULL as "Functional_impact",
                        NULL as "Mutant_allele_status",
                        NULL as "Fusion_chrom_b",
                        NULL as "Fusion_gene_b",
                        NULL as "Fusion_position_a",
                        NULL as "Fusion_position_b",
                        NULL as "Direction_a",
                        NULL as "Direction_b",
                        NULL as "Downstream_gene",
                        NULL as "Copy_number",
                        NULL as "Mol_count",
                        NULL as "Indel_type",
                        NULL as "Alleletype"
                FROM u_ghsinglemethyl srm
                    INNER JOIN RUOREPORTMODULES ON LIMSTABLENAME = 'U_GHSINGLEMETHYL' AND
                        FIELDNAMES = 'gene' AND FIELDVALUES = srm.gene
                WHERE ISPOSITIVEBYREVIEW = '1' and RUOREPORTABLE = 1 and exists (SELECT SampleID from ACTIVESAMPLES where srm.sampleid = ACTIVESAMPLES."SampleID" and srm.runid = ACTIVESAMPLES."RunID")
            ),
            ALLELETYPE AS (
                SELECT 'U_GHALLELETYPE' as LIMSTABLENAME,
                        RUOREPORTMODULES.VARIANTTYPE as "Variant_type",
                        sampleid as "SAMPLE_ID",
                        runid as "RUN_ID",
                        gene as "Gene",
                        NULL as "Chromosome",
                        NULL AS "Position",
                        NULL as "Exon",
                        0 as "Alt-NoAlt",
                        NULL as "Mut_nt",
                        NULL as "Mut_aa",
                        NULL as "Splice_effect",
                        NULL as "Mut_cdna",
                        NULL as "Transcript",
                        NULL as "Percentage",
                        'germline' as "Somatic_status",
                        NULL as "Molecular_consequence",
                        NULL as "CNV_type",
                        NULL as "COSMIC",
                        NULL as "dbSNP",
                        NULL as "ClinVar",
                        NULL as "ClinVarID",
                        NULL as "Functional_impact",
                        NULL as "Mutant_allele_status",
                        NULL as "Fusion_chrom_b",
                        NULL as "Fusion_gene_b",
                        NULL as  "Fusion_position_a",
                        NULL as "Fusion_position_b",
                        NULL as "Direction_a",
                        NULL as "Direction_b",
                        NULL as "Downstream_gene",
                        NULL as "Copy_number",
                        NULL as "Mol_count",
                        NULL as "Indel_type",
                        alleletype as "Alleletype"
                FROM u_ghalleletype
                INNER JOIN RUOREPORTMODULES ON LIMSTABLENAME = 'U_GHALLELETYPE'
                WHERE /*ISPOSITIVEBYREVIEW = '1' and*/ RUOREPORTABLE = 1 and exists (SELECT SampleID from ACTIVESAMPLES where u_ghalleletype.sampleid = ACTIVESAMPLES."SampleID" and u_ghalleletype.runid = ACTIVESAMPLES."RunID")
            ),
            VIRUS AS (
                SELECT 'U_GHVIRUS' as LIMSTABLENAME,
                        RUOREPORTMODULES.VARIANTTYPE as "Variant_type",
                        sampleid as "SAMPLE_ID",
                        runid as "RUN_ID",
                        virus as "Gene",
                        NULL as "Chromosome",
                        NULL AS "Position",
                        NULL as "Exon",
                        0 as "Alt-NoAlt",
                        NULL as "Mut_nt",
                        NULL as "Mut_aa",
                        NULL as "Splice_effect",
                        NULL as "Mut_cdna",
                        NULL as "Transcript",
                        NULL as "Percentage",
                        NULL as "Somatic_status",
                        NULL as "Molecular_consequence",
                        NULL as "CNV_type",
                        NULL as "COSMIC",
                        NULL as "dbSNP",
                        NULL as "ClinVar",
                        NULL as "ClinVarID",
                        NULL as "Functional_impact",
                        NULL as "Mutant_allele_status",
                        NULL as "Fusion_chrom_b",
                        NULL as "Fusion_gene_b",
                        NULL as "Fusion_position_a",
                        NULL as "Fusion_position_b",
                        NULL as "Direction_a",
                        NULL as "Direction_b",
                        NULL as "Downstream_gene",
                        NULL as "Copy_number",
                        NULL as "Mol_count",
                        NULL as "Indel_type",
                        NULL as "Alleletype"
                FROM u_ghvirus
                INNER JOIN RUOREPORTMODULES ON LIMSTABLENAME = 'U_GHVIRUS'
                WHERE ISPOSITIVEBYREVIEW = '1' and RUOREPORTABLE = 1 and exists (SELECT SampleID from ACTIVESAMPLES where u_ghvirus.sampleid = ACTIVESAMPLES."SampleID" and u_ghvirus.runid = ACTIVESAMPLES."RunID")
            ),
            VARIANT AS (
                SELECT * FROM SNV
                UNION
                SELECT * FROM CNV
                UNION
                SELECT * FROM DELETION
                UNION
                SELECT * FROM INDEL
                UNION
                SELECT * FROM FUSION
                UNION
                SELECT * FROM DENOVOFUSION
                UNION
                SELECT * FROM SINGLEREGIONMETHYL
                UNION
                SELECT * FROM ALLELETYPE
                UNION
                SELECT * FROM VIRUS
            ),
            ALT AS (
                 SELECT variant."SAMPLE_ID",
                        variant."RUN_ID",
                        (CASE WHEN count(*) > 0 THEN 1
                            ELSE 0 END) AS "Alt-NoAlt"
                 FROM VARIANT
                 WHERE (VARIANT.LIMSTABLENAME = 'U_GHCNVGENE' OR VARIANT.LIMSTABLENAME = 'U_GHFUSION'
                       OR VARIANT.LIMSTABLENAME = 'U_GHSNV' OR VARIANT.LIMSTABLENAME = 'U_GHINDEL' OR VARIANT.LIMSTABLENAME = 'U_GHDENOVOFUSION') AND VARIANT."Alt-NoAlt" = 1
                 GROUP BY variant."SAMPLE_ID", variant."RUN_ID"
            ),
            BIOMARKER AS (
                SELECT
                    ACTIVESAMPLES."SampleID",
                    ACTIVESAMPLES."RunID",
                    TO_CHAR(ROUND(hrdmod.hrd_score,3)) as "HRD_score",
                    ROUND(hrdbase.msaf, 2)*100 as "Max_percentage",
                    (CASE WHEN sm."call" is NULL THEN NULL WHEN sm."call" = 'Not Detected' THEN sm."call" ELSE 'Detected' END) as "Tumor_methylation_status",
                    sm."predtf" as "Tumor_methylation_score",
                    TO_CHAR(tmb_score) as "TMB_score",
                    tmb_category as "TMB_category",
                    (CASE WHEN msi.msi_status =  'MSI-H' THEN 'Detected' ELSE 'Not Detected' END) as "MSI_High"
                FROM ACTIVESAMPLES
                LEFT JOIN u_ghtmb tmb on ACTIVESAMPLES."SampleID" = tmb.sampleid and ACTIVESAMPLES."RunID" = tmb.runid and 'U_GHTMB' IN (select distinct LIMSTABLENAME from RUOREPORTMODULES)
                LEFT JOIN u_ghmsi msi on ACTIVESAMPLES."SampleID" = msi.sampleid and ACTIVESAMPLES."RunID" = msi.runid and 'U_GHMSI' IN (select distinct LIMSTABLENAME from RUOREPORTMODULES)
                LEFT JOIN u_ghhrd hrdbase on ACTIVESAMPLES."SampleID" = hrdbase.sampleid and ACTIVESAMPLES."RunID" = hrdbase.runid
                LEFT JOIN u_ghhrd hrdmod on ACTIVESAMPLES."SampleID" = hrdmod.sampleid and ACTIVESAMPLES."RunID" = hrdmod.runid and 'U_GHHRD' IN (select distinct LIMSTABLENAME from RUOREPORTMODULES)
                LEFT JOIN (select sampleid, runid, LISTAGG(DISTINCT (CASE WHEN call = 1 THEN 'Detected' ELSE 'Not Detected' END), ';') as "call", LISTAGG(DISTINCT predtf) as "predtf" from u_ghsamplemethyl group by sampleid, runid order by "call" ) sm
                    on ACTIVESAMPLES."SampleID" = sm.sampleid and ACTIVESAMPLES."RunID" = sm.runid and 'U_GHSAMPLEMETHYL' IN (select distinct LIMSTABLENAME from RUOREPORTMODULES)
            )
            SELECT distinct
                vv."Study_ID",
                vv."Customer_SampleID",
                vv."GHRequestID",
                v."SAMPLE_ID" as "GHSampleID",
                vv."Patient_ID",
                vv."Visit_name",
                NVL(a."Alt-NoAlt", 0) as "Alt-NoAlt",
                vv."Sample_status",
                vv."Sample_comment",
                v."Variant_type",
                v."Indel_type",
                v."Gene",
                v."Chromosome",
                v."Position",
                v."Exon",
                v."Mut_aa",
                v."Mut_nt",
                v."Mut_cdna",
                v."Transcript",
                v."Percentage",
                v."Splice_effect",
                v."Somatic_status",
                v."Molecular_consequence",
                v."Fusion_chrom_b",
                v."Fusion_gene_b",
                v."Fusion_position_a",
                v."Fusion_position_b",
                v."Direction_a",
                v."Direction_b",
                v."Downstream_gene",
                v."Copy_number",
                v."CNV_type",
                v."COSMIC",
                v."dbSNP",
                v."ClinVar",
                v."ClinVarID",
                v."Functional_impact",
                v."Mutant_allele_status",
                v."Mol_count",
                b."Max_percentage",
                v."Alleletype",
                b."HRD_score",
                b."Tumor_methylation_status",
                b."Tumor_methylation_score",
                b."TMB_score",
                b."TMB_category",
                b."MSI_High",
                vv."cfDNA_ng",
                vv."Plasma_ml_input",
                vv."Plasma_ml_remaining",
                vv."Received_date",
                vv."Bloodcoll_date",
                vv."Reported_date",
                vv."Cancertype",
                vv."Practice_name",
                vv."Physician_name"
            FROM variant v
                     left join BIOMARKER b on v.SAMPLE_ID = b."SampleID" and v.RUN_ID = b."RunID"
                     left join ALT a on v.SAMPLE_ID = a."SAMPLE_ID" and v.RUN_ID = a."RUN_ID"
                     inner join  V_RESEARCH_REQUEST_INFO vv on vv."SampleID" = v.SAMPLE_ID and vv."RunID" = v.RUN_ID
            order by  vv."GHRequestID";

