# UCloud Ufile SDK for Java

[![](https://img.shields.io/github/release/ucloud/ufile-sdk-java.svg)](https://github.com/ucloud/ufile-sdk-java)
[![](https://img.shields.io/github/last-commit/ucloud/ufile-sdk-java.svg)](https://github.com/ucloud/ufile-sdk-java)
[![](https://img.shields.io/github/commits-since/ucloud/ufile-sdk-java/latest.svg)](https://github.com/ucloud/ufile-sdk-java)

## Version History
- ~~[Ver 1.0.0](https://github.com/ufilesdk-dev/ufile-javasdk)~~ 不建议使用

## 环境要求
- 开发环境: Java 1.7或以上
- 运行环境: Java 1.7或以上

## API Doc

- **[ufile-client-java ](https://github.com/ucloud/ufile-sdk-java/tree/master/ufile/ufile-client-java/apidocs.zip)**
    - **[ufile-core ](https://github.com/ucloud/ufile-sdk-java/tree/master/ufile/ufile-core/apidocs.zip)**
    
## 安装
- Maven

    您可以通过在pom.xml中添加以下依赖项，来配置您的Maven项目

    ``` xml
    <dependency>
        <groupId>cn.ucloud.ufile</groupId>
        <artifactId>ufile-client-java</artifactId>
        <version>2.5.0</version>
    </dependency>
    ```

- Gradle

    ``` java
    dependencies {
        /*
         * your other dependencies
         */
        implementation 'cn.ucloud.ufile:ufile-client-java:2.5.0'
    }
    ```

## 快速入门

- 基本说明：
    - 所有Ufile所有API均包含同步执行(execute)和异步执行(executeAsync)两种执行方式。
    
    - 同步执行会返回指定的业务结果类，若执行出错则会抛出UfileException为父类的异常；
    
    - 异步执行需要传入UfileCallback<T>的回调接口，执行成功时会回调onResponse，泛型<T>为回调结果(即：同步执行的返回类型)，**值得注意的是，若Ufile Server业务错误，也会回调onResponse，请注意结果类中的信息**，若出现异常，则回调onError。
    
    - 如果是上传下载等耗时API，建议使用异步执行(executeAsync)，并可以重写UfileCallback中的onProgress回调来进行进度监听

## 配置UfileClient

- 必须在使用UfileClient之前调用，即：必须是UfileClient第一个调用的方法才有效。否则使用默认UfileClient.Config

    ``` java
    UfileClient.configure(new UfileClient.Config(
                    new HttpClient.Config(int maxIdleConnections, long keepAliveDuration, TimeUnit keepAliveTimeUnit)
                            .setTimeout(连接超时ms，读取超时ms，写入超时ms)
                            .setExecutorService(线程池)
                            .addInterceptor(okhttp3拦截器)
                            .addNetInterceptor(okhttp3网络拦截器)));
    ```

### Bucket相关操作
``` java
// Bucket相关API的授权器
BucketAuthorization BUCKET_AUTHORIZER = new UfileBucketLocalAuthorization(
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
    } catch (UfileClientException e) {
        e.printStackTrace();
    } catch (UfileServerException e) {
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

##### 关于ObjectConfig的region参数，是指您的bucket所创建在的地区编码，请参考[UCloud 地区列表](https://docs.ucloud.cn/api/summary/regionlist.html)

``` java
// 对象相关API的授权器
ObjectAuthorization OBJECT_AUTHORIZER = new UfileObjectLocalAuthorization(
            "Your PublicKey", "Your PrivateKey");
            
/**
 * 您也可以创建远程对象相关API的授权器，远程授权器将签名私钥放于签名服务器上，更为安全
 * 远程签名服务端示例代码在 (https://github.com/ucloud/ufile-sdk-auth-server)
 * 您也可以自行继承ObjectRemoteAuthorization来重写远程签名逻辑
 */
ObjectAuthorization OBJECT_AUTHORIZER = new UfileObjectRemoteAuthorization(
            您的公钥,
            new ObjectRemoteAuthorization.ApiConfig(
                    "http://your_domain/applyAuth",
                    "http://your_domain/applyPrivateUrlAuth"
            ));
// 对象操作需要ObjectConfig来配置您的地区和域名后缀
ObjectConfig config = new ObjectConfig("your bucket region", "ufileos.com");

/** 
 * 您也可以使用已登记的自定义域名
 * 注意'http://www.your_domain.com'指向的是某个特定的bucket+region+域名后缀，
 * eg：http://www.your_domain.com -> www.your_bucket.bucket_region.ufileos.com
 */
ObjectConfig config = new ObjectConfig("http://www.your_domain.com");

/**
 * ObjectConfig同时支持从本地文件来导入
 * 配置文件内容必须是含有以下参数的json字符串：
 *     {"Region":"","ProxySuffix":""} 
 *     或
 *     {"CustomDomain":""}
 */
 try {
     ObjectConfig.loadProfile(new File("your config profile path"));
 } catch (UfileFileException e) {
     e.printStackTrace();
 }

UfileClient.object(OBJECT_AUTHORIZER, config)
    .APIs           // 对象存储相关API
    .execute() or executeAsync(UfileCallback<T>)
```

##### 上传文件

- 同步

    ``` java
    File file = new File("your file path");
    
    try {
        PutObjectResultBean response = UfileClient.object(Constants.OBJECT_AUTHORIZER, config)
             .putObject(file, "mimeType")
             .nameAs("save as keyName")
             .toBucket("upload to which bucket")
             /**
              * 是否上传校验MD5, Default = true
              */
         //  .withVerifyMd5(false)
             /**
              * 指定progress callback的间隔, Default = 每秒回调
              */
         //  .withProgressConfig(ProgressConfig.callbackWithPercent(10))
             /**
              * 配置进度监听
              */
             .setOnProgressListener(new OnProgressListener() {
                  @Override
                  public void onProgress(long bytesWritten, long contentLength) {
                      
                  }
             })
             .execute();
    } catch (UfileClientException e) {
        e.printStackTrace();
    } catch (UfileServerException e) {
        e.printStackTrace();
    }
    ```

- 异步

    ``` java
    File file = new File("your file path");
    
    UfileClient.object(OBJECT_AUTHORIZER, config)
         .putObject(file, "mimeType")
         .nameAs("save as keyName")
         .toBucket("upload to which bucket")
         /**
          * 是否上传校验MD5, Default = true
          */
    //   .withVerifyMd5(false)
         /**
          *指定progress callback的间隔, Default = 每秒回调
          */
    //   .withProgressConfig(ProgressConfig.callbackWithPercent(10))
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

