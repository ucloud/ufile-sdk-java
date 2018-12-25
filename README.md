# UCloud Ufile SDK for Java

[![](https://img.shields.io/github/release/ucloud/ufile-sdk-java.svg)](https://github.com/ucloud/ufile-sdk-java)

## Version History
- ~~[Ver 1.0.0](https://github.com/ufilesdk-dev/ufile-javasdk)~~ 不建议使用

## 环境要求
- Java 1.8或以上

## 安装
- Maven

    您可以通过在pom.xml中添加以下依赖项，来配置您的Maven项目

    ``` xml
    <dependency>
        <groupId>cn.ucloud.ufile</groupId>
        <artifactId>ufile-client-java</artifactId>
        <version>2.0.1</version>
    </dependency>
    ```

- JCenter
    暂未提交JCenter仓库

## 快速入门

- 基本说明：
    - 所有Ufile所有API均包含同步执行(execute)和异步执行(executeAsync)两种执行方式。
    
    - 同步执行会返回指定的业务结果类，若执行出错则会抛出UfileException为父类的异常；
    
    - 异步执行需要传入UfileCallback<T>的回调接口，执行成功时会回调onResponse，泛型<T>为回调结果(即：同步执行的返回类型)，**值得注意的是，若Ufile Server业务错误，也会回调onResponse，请注意结果类中的信息**，若出现异常，则回调onError。
    
    - 如果是上传下载等耗时API，建议使用异步执行(executeAsync)，并可以重写UfileCallback中的onProgress回调来进行进度监听

### Bucket相关操作
``` java
// Bucket相关API的授权器
UfileBucketLocalAuthorization BUCKET_AUTHORIZER = new UfileBucketLocalAuthorization(
            "Your PublicKey", "Your PrivateKey");
            
UfileClient.bucket(BUCKET_AUTHORIZER)
    .APIs       // Bucket相关操作API
    .execute() or executeAsync(UfileCallback<T>)
```
##### 创建Bucket

- 同步

``` java
try {
    BucketResponse res = UfileClient.bucket(BUCKET_AUTHORIZER)
        .createBucket(bucketName, region, bucketType)
        .execute();
} catch (UfileException e) {
    e.printStackTrace();
}
```
- 异步

``` java
UfileClient.bucket(BUCKET_AUTHORIZER)
    .createBucket(bucketName, region, bucketType)
    .executeAsync(new UfileCallback<BucketResponse>() {
        @Override
        public void onResponse(BucketResponse response) {
            
        }
    
        @Override
        public void onError(Request request, ApiError error, UfileErrorBean response) {
            
        }
});
```

### 对象相关操作
``` java
// 对象相关API的授权器
UfileObjectLocalAuthorization OBJECT_AUTHORIZER = new UfileObjectLocalAuthorization(
            "Your PublicKey", "Your PrivateKey");
// 对象操作需要ObjectConfig来配置您的地区和域名后缀
ObjectConfig config = new ObjectConfig("your bucket region", "ufileos.com");

/** 您也可以使用已登记的自定义域名
 * 注意'http://www.your_domain.com'指向的是某个特定的bucket+region+域名后缀，
 * eg：http://www.your_domain.com -> www.your_bucket.bucket_region.ufileos.com
 */
// ObjectConfig config = new ObjectConfig("http://www.your_domain.com");

/**
 * ObjectConfig同时支持从本地文件来导入
 * 配置文件内容必须是含有以下参数的json字符串：
 *     {"Region":"","ProxySuffix":""} 
 *     或
 *     {"CustomDomain":""}
 */
 /*
    try {
        ObjectConfig.loadProfile(new File("your config profile path"));
    } catch (UfileFileException e) {
        e.printStackTrace();
    }
*/

UfileClient.object(OBJECT_AUTHORIZER, config)
    .APIs           // 对象存储相关API
    .execute() or executeAsync(UfileCallback<T>)
```

##### 上传文件

``` java
File file = new File("your file path");
String mimeType = "mimeType";
String keyName = "save as keyName";
String bucketName = "upload to which bucket";

UfileClient.object(OBJECT_AUTHORIZER, config)
        .putObject(file, mimeType)
        .nameAs(keyName)
        .toBucket(bucketName)
        /**
         * 是否上传校验MD5
         */
//       .withVerifyMd5(false)
        /**
         * 指定progress callback的间隔
         */
//       .withProgressConfig(ProgressConfig.callbackWithPercent(10))
        .executeAsync(new UfileCallback<PutObjectResultBean>() {
            @Override
            public void onProgress(long bytesWritten, long contentLength) {
                
            }

            @Override
            public void onResponse(PutObjectResultBean response) {
                
            }

            @Override
            public void onError(Request request, ApiError error, UfileErrorBean response) {
                
            }
        });
```

## License
[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html)

## 作者
- [Joshua Yin](https://github.com/joshuayin)

