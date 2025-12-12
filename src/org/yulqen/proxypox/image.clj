(ns org.yulqen.proxypox.image
  (:require [org.httpkit.client :as http]
            [clojure.java.io :as io])
  (:import [javax.imageio ImageIO]
           [java.io ByteArrayInputStream ByteArrayOutputStream]
           [java.awt AlphaComposite]))

(defn read-image-from-file
  "Reads a file to bytes."
  [path]
  (let [image-data (-> path io/file .toPath java.nio.file.Files/readAllBytes)]
    (if image-data
      (ImageIO/read (ByteArrayInputStream. image-data))
      (println "Cannot read image data."))))

(defn read-image-from-url [url]
  (let [response @(http/get url {:as :byte-array})]
    (if-let [err (:error response)]
      (println "HTTP request failed:" err)
      (if (= 200 (:status response))
        (let [image-data (:body response)]
          (ImageIO/read (ByteArrayInputStream. image-data)))
        (println "Request returned non-200 status:" (:status response))))))

(defn apply-watermark
  "Applies a watermark-image onto the base-image at coordinates (x, y)."  
  [base-image watermark-image x y]
  (let [g2d (.createGraphics base-image)]
    (.setComposite g2d (AlphaComposite/getInstance AlphaComposite/SRC_OVER (float 0.5)))
    (.drawImage g2d watermark-image x y nil)
    (.dispose g2d)
    base-image))

(defn just-image-from-watermark-file
  [base-url watermark-file-path]
  (let [base-image (read-image-from-url base-url)
        watermark-image (read-image-from-file watermark-file-path)]
    (when (and base-image watermark-image)
      (let [watermarked-image (apply-watermark base-image watermark-image 100 100)
            baos (ByteArrayOutputStream.)]
        (ImageIO/write watermarked-image "png" baos)
        (.toByteArray baos)))))

(defn just-image
  "Reads base and watermark images from URLs, applies the watermark, and returns the result as a byte array."
  [base-url watermark-url]
  (let [base-image (read-image-from-url base-url)
        watermark-image (read-image-from-url watermark-url)]
    (when (and base-image watermark-image)
      (let [watermarked-image (apply-watermark base-image watermark-image 0 0)
            baos (ByteArrayOutputStream.)]
        (ImageIO/write watermarked-image "png" baos)
        (.toByteArray baos)))))

#_(defn save-image [image filename]
  (ImageIO/write image "png" (File. filename)))

#_(defn wm-image [base watermark]
    (let [base-image (read-image-from-url base)
          watermark-image (read-image-from-url watermark)
          watermarked-image (apply-watermark base-image watermark-image 0 0)]
      (save-image watermarked-image "/tmp/WATERMARKED_IMAGE.png")
      (println "Image with watermark saved!")))

