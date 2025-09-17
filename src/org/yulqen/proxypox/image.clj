(ns org.yulqen.proxypox.image
  (:require [org.httpkit.client :as http]))

(import '[javax.imageio ImageIO])
(import '[java.io ByteArrayInputStream ByteArrayOutputStream])
(import '[java.awt Graphics2D AlphaComposite])
(import '[java.io File])

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
  (let [g2d (.createGraphics base-image)
        watermark-width (.getWidth watermark-image)
        watermark-height (.getHeight watermark-image)]
    (.setComposite g2d (AlphaComposite/getInstance AlphaComposite/SRC_OVER (float 0.5)))
    (.drawImage g2d watermark-image x y nil)
    (.dispose g2d)
    base-image))

(defn save-image [image filename]
  (ImageIO/write image "png" (File. filename)))

(defn just-image
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

