(ns org.yulqen.proxypox.aws
  (:require [cognitect.aws.client.api :as aws]
            [cognitect.aws.credentials :as credentials]
            [org.yulqen.proxypox.utils :as utils]))

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
    (fetch-image-from-spaces (:aws-bucket-name (utils/grab-environment)) full-key)))
