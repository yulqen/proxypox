(ns org.yulqen.proxypox.core)

(require '[clj-http.client :as client])

(import '[javax.imageio ImageIO])
(import '[java.io ByteArrayInputStream])
(import '[java.awt Graphics2D AlphaComposite])
(import '[java.io File])

(defn read-image-from-url [url]
  (try
    (let [response (client/get url {:as :byte-array})
          image-data (:body response)]
      (ImageIO/read (ByteArrayInputStream. image-data)))
    (catch Exception e
      (println "Error reading image: " (.getMessage e))
      nil)))

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

(def base-url "https://yulqen.org/img/poser_guy.png")
(def watermark-url "https://alphabetlearning.online/static/images/AL_long_logo_black_grey_750.1ec1231fe406.png")

(defn save-image [image filename]
  (ImageIO/write image "png" (File. filename)))

(defn wm-image [base watermark]
  (let [base-image (read-image-from-url base)
        watermark-image (read-image-from-url watermark)
        watermarked-image (apply-watermark base-image watermark-image 50 10)]
    (save-image watermarked-image "/tmp/WATERMARKED_IMAGE.png")
    (println "Image with watermark saved!")))

(comment
  (read-image-from-url "https://guzzler.alphabetlearning.online/A5ZoW17WYcksx6OpINEVSvzU3zXI4DuzHngzyR-2-7c/f:webp/g:ce/czM6Ly9qbC1yZXNvdXJjZXMvdGh1bWJuYWlscy9BdXR1bW4gd29yZHMgd29yZHNlYXJjaCBDb3ZlciBJbWFnZS5qcGc"))
