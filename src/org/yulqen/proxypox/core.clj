(ns org.yulqen.proxypox.core)

(require '[org.httpkit.client :as http])

(import '[javax.imageio ImageIO])
(import '[java.io ByteArrayInputStream])
(import '[java.awt Graphics2D AlphaComposite])
(import '[java.io File])

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

(comment
  (defn report-image-size [img-url]
    (let [image (read-image-from-url img-url)]
      (if image
        (println "Successfully read image with dimensions:"
                 (.getWidth image) "x" (.getHeight image)))))
  )
