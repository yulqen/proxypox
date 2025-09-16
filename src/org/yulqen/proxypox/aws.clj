(ns org.yulqen.proxypox.aws
  (:require [cognitect.aws.client.api :as aws]
            [cognitect.aws.credentials :as credentials]))

(defn- grab-environment
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

; endpoint: https://jl-resources.ams3.digitaloceanspaces.com/dev_thumbnails/Alphabet%20Letters%20Initial%20Sound%20Match%20Order%20Hh%20Cover%20Image.jpg

(def s3-client
  (aws/client
   {:api                  :s3
    :region               "eu-west-1" ;; <-- Your space's region
    :endpoint-override    {:protocol :https
                           :hostname "ams3.digitaloceanspaces.com" ;; <-- Your space's endpoint
                           :port     443}}))

(def s3-path-prefix "tangible_photos/")

(defn get-full-key [object-key]
  (str s3-path-prefix object-key))


(defn fetch-image-from-spaces
  "Fetches an object from DO Spaces and returns its InputStream."
  [bucket-name object-key]
  (let [response (aws/invoke s3-client
                             {:op      :GetObject
                              :request {:Bucket bucket-name
                                        :Key    object-key}})]
    ;; Check for errors, which Cognitect's library returns as anomalies
    (if (:cognitect.anomalies/category response)
      (throw (ex-info "Failed to fetch from S3" {:response response}))
      ;; The raw image data is in the :Body as an InputStream
      (:Body response))))

; eval this...
(defn get-key-from-s3 [image-key]
  (let [full-key (get-full-key image-key)]
    (fetch-image-from-spaces (:aws-bucket-name (grab-environment)) full-key)))
