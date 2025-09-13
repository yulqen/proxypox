(ns org.yulqen.proxypox.core
  (:require [org.httpkit.client :as http]))

(import '[javax.imageio ImageIO])
(import '[java.io ByteArrayInputStream])
(import '[java.awt Graphics2D AlphaComposite])
(import '[java.io File])
(import '[java.util Base64])

(defn read-image-from-url [url]
  (let [response @(http/get url {:as :byte-array})]
    (if-let [err (:error response)]
      (println "HTTP request failed:" err) ; Or throw an exception
      (if (= 200 (:status response))
        (let [image-data (:body response)]
          (ImageIO/read (ByteArrayInputStream. image-data)))
        (println "Request returned non-200 status:" (:status response))))))


(defn -main []
  (println "Hello,world!"))

(defn report-image-size [img-url]
  (let [image (read-image-from-url img-url)]
    (if image
      (println "Successfully read image with dimensions:"
               (.getWidth image) "x" (.getHeight image)))))


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

(defn wm-image [base watermark]
  (let [base-image (read-image-from-url base)
        watermark-image (read-image-from-url watermark)
        watermarked-image (apply-watermark base-image watermark-image 0 0)]
    (save-image watermarked-image "/tmp/WATERMARKED_IMAGE.png")
    (println "Image with watermark saved!")))

(def object_key "Counting in 5s cards to 50 x7 colours Starfish AL.pdf_006.jpg")
(def s3_storage_name "jl-resources")
(def s3_prefix "dev")

(defn test-string-concat []
  (let [url-string (apply str "s3://" s3_storage_name "/" s3_prefix "/" object_key)]
    url-string))

(defn encode-url [url]
  (let [s-enc (.encodeToString (Base64/getEncoder) (.getBytes url))]
    (println s-enc)))

(defn decode-url [b64-url]
  (new String (.decode (Base64/getDecoder) b64-url)))


(comment
  (defn report-image-size [img-url]
    (let [image (read-image-from-url img-url)]
      (if image
        (println "Successfully read image with dimensions:"
                 (.getWidth image) "x" (.getHeight image)))))
  )
