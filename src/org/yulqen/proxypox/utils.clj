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

#_(defn encode-url
  "Encodes a URL using URL-safe Base64 without padding."
  [url]
  (let [encoder (-> (Base64/getUrlEncoder) (.withoutPadding))]
    (.encodeToString encoder (.getBytes url java.nio.charset.StandardCharsets/UTF_8))))

(defn decode-url
  "Decodes a URL-safe Base64 string (with or without padding)."
  [b64-url]
  (let [decoder (Base64/getUrlDecoder)]
    (String. (.decode decoder b64-url) java.nio.charset.StandardCharsets/UTF_8)))

#_(defn- hex-to-bytes [hex-str]
  (let [len (.length hex-str)]
    (byte-array
      (for [i (range 0 len 2)]
        (unchecked-byte (Integer/parseInt (subs hex-str i (+ i 2)) 16))))))

#_(defn test-string-concat []
  (let [url-string (apply str "s3://" (:aws-bucket-name (grab-environment))
                          "/"
                          "dev"
                          "/"
                          "Counting in 5s cards to 50 x7 colours Starfish AL.pdf_006.jpg")]
    url-string))

#_(comment
  (defn report-image-size [img-url]
    (let [image (image/read-image-from-url img-url)]
      (if image
        (println "Successfully read image with dimensions:"
                 (.getWidth image) "x" (.getHeight image)))))
  )
