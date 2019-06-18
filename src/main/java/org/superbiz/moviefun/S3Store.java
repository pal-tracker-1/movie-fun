package org.superbiz.moviefun;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.IOException;
import java.util.Optional;

public class S3Store implements BlobStore {
    private final AmazonS3Client s3Client;
    private final String photoStorageBucket;

    public S3Store(AmazonS3Client s3Client, String photoStorageBucket) {
        this.s3Client = s3Client;
        this.photoStorageBucket = photoStorageBucket;
    }

    @Override
    public void put(Blob blob) {
        if (!s3Client.doesBucketExist(photoStorageBucket)) {
            s3Client.createBucket(photoStorageBucket);
        }
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType(blob.getContentType());
        s3Client.putObject(photoStorageBucket, blob.getName(), blob.getInputStream(), meta);
    }

    @Override
    public Optional<Blob> get(String name) {
        if (!s3Client.doesBucketExist(photoStorageBucket)) {
            s3Client.createBucket(photoStorageBucket);
        }
        if (s3Client.doesObjectExist(photoStorageBucket, name)) {
            S3Object s3 = s3Client.getObject(photoStorageBucket, name);
            Blob b = new Blob(name, s3.getObjectContent(), s3.getObjectMetadata().getContentType());
            return Optional.of(b);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void deleteAll() {
        s3Client.deleteBucket(photoStorageBucket);
        s3Client.createBucket(photoStorageBucket);
    }
}
