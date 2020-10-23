package uk.gov.hmcts.reform.locationrefdata.cameltest.testsupport;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.data.ingestion.configuration.AzureBlobConfig;
import uk.gov.hmcts.reform.data.ingestion.configuration.BlobStorageCredentials;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.PostConstruct;

import static com.microsoft.azure.storage.blob.DeleteSnapshotsOption.INCLUDE_SNAPSHOTS;
import static java.util.Objects.isNull;

@Component
@ContextConfiguration(classes = {
    AzureBlobConfig.class, BlobStorageCredentials.class}, initializers = ConfigFileApplicationContextInitializer.class)
public class LrdBlobSupport {


    @Autowired
    @Qualifier("credscloudStorageAccount")
    CloudStorageAccount acc;

    CloudBlobClient cloudBlobClient;

    CloudBlobContainer cloudBlobContainer;

    CloudBlobContainer cloudBlobArchContainer;

    @Value("${archival-date-format}")
    private String archivalDateFormat;

    @PostConstruct
    public void init() throws Exception {
        cloudBlobClient = acc.createCloudBlobClient();
        cloudBlobContainer = cloudBlobClient.getContainerReference("lrd-ref-data");
        cloudBlobArchContainer = cloudBlobClient.getContainerReference("lrd-ref-data-archive");
    }

    public void uploadFile(String blob, InputStream sourceFile) throws Exception {
        CloudBlockBlob cloudBlockBlob = cloudBlobContainer.getBlockBlobReference(blob);
        cloudBlockBlob.upload(sourceFile, 8 * 1024 * 1024);
    }

    public void deleteBlob(String blob, boolean... status) throws Exception {
        CloudBlockBlob cloudBlockBlob = cloudBlobContainer.getBlockBlobReference(blob);
        cloudBlockBlob.delete(INCLUDE_SNAPSHOTS, null, null, null);
        String date = new SimpleDateFormat(archivalDateFormat).format(new Date());

        //Skipped for Stale non existing files as not archived
        if (isNull(status)) {
            cloudBlockBlob = cloudBlobArchContainer.getBlockBlobReference(blob.concat(date));
            cloudBlockBlob.delete(INCLUDE_SNAPSHOTS, null, null, null);
        }
    }
}
