(ns org.yulqen.proxypox.core
  (:require [org.httpkit.client :as http]
            [org.httpkit.server :as server]
            [compojure.core :refer [defroutes GET]] ; Corrected the syntax for Compojure
            [compojure.route :as route]))

(import '[javax.imageio ImageIO])
(import '[java.io ByteArrayInputStream ByteArrayOutputStream])
(import '[java.awt Graphics2D AlphaComposite])
(import '[java.io File])
(import '[java.util Base64])
(import '[javax.crypto Mac])
(import java.nio.charset.StandardCharsets)

;; This atom will hold the server instance so we can start and stop it.
;; `defonce` ensures it's only defined once, which is good for REPL usage.
(defonce server-instance (atom nil))

;; --- Helper functions for image processing, placed before the app definition ---

(defn read-image-from-url [url]
  (let [response @(http/get url {:as :byte-array})]
    (if-let [err (:error response)]
      (println "HTTP request failed:" err)
      (if (= 200 (:status response))
        (let [image-data (:body response)]
          (ImageIO/read (ByteArrayInputStream. image-data)))
        (println "Request returned non-200 status:" (:status response))))))

(defn apply-watermark [base-image watermark-image x y]
  "Applies a watermark-image onto the base-image at coordinates (x, y)."
  (let [g2d (.createGraphics base-image)
        watermark-width (.getWidth watermark-image)
        watermark-height (.getHeight watermark-image)]
    (.setComposite g2d (AlphaComposite/getInstance AlphaComposite/SRC_OVER (float 0.5)))
    (.drawImage g2d watermark-image x y nil)
    (.dispose g2d)
    base-image))

(defn save-image [image filename]
  (ImageIO/write image "png" (File. filename)))

(defn- just-image
  "Reads base and watermark images from URLs, applies the watermark, and returns the result as a byte array."
  [base watermark]
  (let [base-image (read-image-from-url base)
        watermark-image (read-image-from-url watermark)]
    (when (and base-image watermark-image)
      (let [watermarked-image (apply-watermark base-image watermark-image 0 0)
            baos (ByteArrayOutputStream.)]
        (ImageIO/write watermarked-image "png" baos)
        (.toByteArray baos)))))

(defn wm-image [base watermark]
  (let [base-image (read-image-from-url base)
        watermark-image (read-image-from-url watermark)
        watermarked-image (apply-watermark base-image watermark-image 0 0)]
    (save-image watermarked-image "/tmp/WATERMARKED_IMAGE.png")
    (println "Image with watermark saved!")))

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


;; --- Main application code ---

(defroutes app
  (GET "/" [] "<h1>Hello, world</h1>")
  
  ;; A new route to handle the base64 encoded URL
  (GET "/image/:b64-url" [b64-url]
    (try
      (let [decoded-url (decode-url b64-url)]
        (println "Received request for image at:" decoded-url)
        (wm-image decoded-url "https://alphabetlearning.online/static/images/AL_long_logo_black_grey_750.1ec1231fe406.png")
        "Image processing started.")
      (catch Exception e
        (str "Error processing request: " (.getMessage e)))))
  
  (GET "/image2/:b64-url" [b64-url]
    (try
      (let [decoded-url (decode-url b64-url)
            image-bytes (just-image decoded-url "https://alphabetlearning.online/static/images/AL_long_logo_black_grey_750.1ec1231fe406.png")]
        (if image-bytes
          {:status 200
           :headers {"Content-Type" "image/png"}
           :body image-bytes}
          {:status 404
           :body "Image not found or could not be processed."}))
      (catch Exception e
        {:status 500
         :body (str "Error processing request: " (.getMessage e))})))
  
  (route/not-found "<h1>Page not found</h1>"))

(defn start-server
  "Starts the HTTP server and stores the instance in the server-instance atom."
  []
  (when-not @server-instance
    (let [s (server/run-server app {:port 9000})]
      (reset! server-instance s)
      (println "Server started on port 9000."))))

(defn stop-server
  "Stop the http server gracefully."
  []
  (when-not (nil? @server-instance)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server-instance :timeout 100)
    (reset! server-instance nil)
    (println "Stopping server.")))


(defn -main []
  (println "Starting the server...")
  (start-server))

(def object_key "Counting in 5s cards to 50 x7 colours Starfish AL.pdf_006.jpg")
(def s3_storage_name "jl-resources")
(def s3_prefix "dev")

(defn test-string-concat []
  (let [url-string (apply str "s3://" s3_storage_name "/" s3_prefix "/" object_key)]
    url-string))


(defn- grab-environment
  []
  {:imgproxy-key (System/getenv "IMGPROXY_KEY")
   :imgproxy-salt (System/getenv "IMGPROXY_SALT")
   :imgproxy-baseurl (System/getenv "IMGPROXY_BASE_URL")
   :imgproxy-bind (System/getenv "IMGPROXY_BIND")
   :imgproxy-region (System/getenv "IMGPROXY_REGION")
   :imgproxy-s3-endpoint (System/getenv "IMGPROXY_S3_ENDPONT")
   :imgproxy-timeout (System/getenv "IMGPROXY_TIMEOUT")
   :imgproxy-use-etag (System/getenv "IMGPROXY_USE_ETAG")
   :imgproxy-use-s3 (System/getenv "IMGPROXY_USE_S3")})

#_(defn sign-path [path-to-sign key-hex salt-hex]
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
    (let [image (read-image-from-url img-url)]
      (if image
        (println "Successfully read image with dimensions:"
                 (.getWidth image) "x" (.getHeight image)))))
  )
