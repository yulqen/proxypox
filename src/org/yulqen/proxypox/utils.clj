(ns org.yulqen.proxypox.utils)
(import '[java.util Base64])

(defn grab-environment
  []
  {:aws-spaces-key (System/getenv "SPACES_KEY")
   :aws-spaces-secret (System/getenv "SPACES_SECRET")
   :aws-bucket-name (System/getenv "SPACES_BUCKET_NAME")
   :imgproxy-key (System/getenv "IMGPROXY_KEY")
   :imgproxy-salt (System/getenv "IMGPROXY_SALT")
   :imgproxy-baseurl (System/getenv "IMGPROXY_BASE_URL")
   :imgproxy-bind (System/getenv "IMGPROXY_BIND")
   :imgproxy-region (System/getenv "IMGPROXY_REGION")
   :imgproxy-s3-endpoint (System/getenv "IMGPROXY_S3_ENDPONT")
   :imgproxy-timeout (System/getenv "IMGPROXY_TIMEOUT")
   :imgproxy-use-etag (System/getenv "IMGPROXY_USE_ETAG")
   :imgproxy-use-s3 (System/getenv "IMGPROXY_USE_S3")})

(defn encode-url
  "Encodes a URL using URL-safe Base64 without padding."
  [url]
  (let [encoder (-> (Base64/getUrlEncoder) (.withoutPadding))]
    (.encodeToString encoder (.getBytes url java.nio.charset.StandardCharsets/UTF_8))))

(defn decode-url
  "Decodes a URL-safe Base64 string (with or without padding)."
  [b64-url]
  (let [decoder (Base64/getUrlDecoder)]
    (String. (.decode decoder b64-url) java.nio.charset.StandardCharsets/UTF_8)))

(defn- hex-to-bytes [hex-str]
  (let [len (.length hex-str)]
    (byte-array
      (for [i (range 0 len 2)]
        (unchecked-byte (Integer/parseInt (subs hex-str i (+ i 2)) 16))))))

(defn test-string-concat []
  (let [url-string (apply str "s3://" s3_storage_name "/" s3_prefix "/" object_key)]
    url-string))

(defn sign-path [path-to-sign key-hex salt-hex]
  (let [key-bytes (hex-to-bytes key-hex)
        salt-bytes (hex-to-bytes salt-hex)
        path-bytes (.getBytes path-to-sign "UTF-8")
        mac (javax.crypto.Mac/getInstance "HmacSHA256")
        key (new javax.crypto.spec.SecretKeySpec key-bytes "HmacSHA256")]
    (.init mac key)
    (.update mac salt-bytes)
    (.update mac path-bytes)
    (let [digest (.doFinal mac)
          encoded-digest (clojure.string/replace
                          (java.util.Base64/getUrlEncoder)
                          (java.util.Base64/encodeToString digest)
                          "=" "")]
      encoded-digest)))

(comment
  (defn report-image-size [img-url]
    (let [image (image/read-image-from-url img-url)]
      (if image
        (println "Successfully read image with dimensions:"
                 (.getWidth image) "x" (.getHeight image)))))
  )
