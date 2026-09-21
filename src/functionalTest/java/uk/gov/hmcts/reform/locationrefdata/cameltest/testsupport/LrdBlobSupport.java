package uk.gov.hmcts.reform.locationrefdata.cameltest.testsupport;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.data.ingestion.configuration.AzureBlobConfig;
import uk.gov.hmcts.reform.data.ingestion.configuration.BlobStorageCredentials;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.azure.storage.blob.models.DeleteSnapshotsOptionType.INCLUDE;
import static java.util.Objects.isNull;

@Component
@ContextConfiguration(classes = {
    AzureBlobConfig.class, BlobStorageCredentials.class}, initializers = ConfigDataApplicationContextInitializer.class)
public class LrdBlobSupport {

    @Autowired
    private BlobServiceClientBuilder blobServiceClientBuilder;

    @Autowired
    private AzureBlobConfig azureBlobConfig;

    BlobServiceClient cloudBlobClient;

    BlobContainerClient cloudBlobContainer;

    BlobContainerClient cloudBlobArchContainer;

    @Value("${archival-date-format}")
    private String archivalDateFormat;

    @PostConstruct
    public void init() throws Exception {
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(
            azureBlobConfig.getAccountName(), azureBlobConfig.getAccountKey());
        String uri = String.format("https://%s.blob.core.windows.net", azureBlobConfig.getAccountName());
        cloudBlobClient = blobServiceClientBuilder
            .endpoint(uri)
            .credential(credential)
            .buildClient();
        cloudBlobContainer = cloudBlobClient.createBlobContainerIfNotExists("lrd-ref-data");
        cloudBlobArchContainer = cloudBlobClient.createBlobContainerIfNotExists("lrd-ref-data-archive");
    }

    public void uploadFile(String blob, InputStream sourceFile) throws Exception {
        BlobClient cloudBlockBlob = cloudBlobContainer.getBlobClient(blob);
        cloudBlockBlob.upload(sourceFile);
    }

    public void deleteBlob(String blob, boolean... status) throws Exception {
        Thread.sleep(1000);
        BlobClient cloudBlockBlob = cloudBlobContainer.getBlobClient(blob);
        if (cloudBlockBlob.exists()) {
            cloudBlockBlob.deleteWithResponse(INCLUDE, null, null, null);
            String date = new SimpleDateFormat(archivalDateFormat).format(new Date());

            //Skipped for Stale non existing files as not archived
            if (isNull(status)) {
                cloudBlockBlob = cloudBlobArchContainer.getBlobClient(blob.concat(date));
                if (cloudBlockBlob.exists()) {
                    cloudBlockBlob.deleteWithResponse(INCLUDE, null, null, null);
                }
            }
        }
    }

    public boolean isBlobPresent(String blob) throws Exception {
        BlobClient cloudBlockBlob = cloudBlobContainer.getBlobClient(blob);
        return cloudBlockBlob.exists();
    }
}
