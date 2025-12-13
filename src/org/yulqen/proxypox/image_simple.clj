(ns org.yulqen.proxypox.image-simple
  (:require [clojure.string :as str])
  (:import
     ;; For handling image data in memory
     [java.awt.image BufferedImage]
     ;; For drawing operations (resizing, watermarking)
     [java.awt Graphics2D RenderingHints]
     ;; For reading from and writing to files and URLs
     [java.io File]
     [java.net URL]
     ;; The main Java Image I/O library
     [javax.imageio ImageIO]))


(defn fetch-image-from-url
  "Fetches an image from a given URL string.

  Parameters:
  - `url-str`: A string representing the URL of the image to fetch.

  Returns:
  - A `java.awt.image.BufferedImage` object containing the image data.

  Throws:
  - `java.io.IOException` if the URL is invalid, the image cannot be read,
    or a network error occurs."
  [url-str]
  (ImageIO/read (URL. url-str)))

(defn read-image-from-file
  "Reads an image from a local file path.

  Parameters:
  - `file-path`: A string representing the path to the local image file
    (e.g., \"/path/to/my/watermark.png\").

  Returns:
  - A `java.awt.image.BufferedImage` object containing the image data.

  Throws:
  - `java.io.IOException` if the file does not exist, cannot be read,
    or is not in a supported format."
  [file-path]
  (ImageIO/read (File. file-path)))

(defn resize-image
  "Resizes a BufferedImage to a new size based on a scale factor.

  This function creates a new, high-quality resized image by drawing the
  original image onto a new canvas with different dimensions.

  Parameters:
  - `original-image`: The `BufferedImage` to resize.
  - `scale-factor`: A double representing the percentage to scale to.
    (e.g., 0.5 for 50%, 1.5 for 150%).

  Returns:
  - A new `BufferedImage` that is the resized version of the original."
  [original-image scale-factor]
  (let [orig-width (.getWidth original-image)
        orig-height (.getHeight original-image)
        new-width (int (* orig-width scale-factor))
        new-height ( int ( * orig-height scale-factor))
        ;; Create a new image with the calculated dimensions
        new-image (BufferedImage. new-width new-height (.getType original-image))
        ;; Get the Graphics2D object to draw on the new image
        g (.createGraphics new-image)]

    ;; Set rendering hints for better quality during the draw operation
    (doto g
      (.setRenderingHint RenderingHints/KEY_INTERPOLATION
                         RenderingHints/VALUE_INTERPOLATION_BILINEAR)
      (.setRenderingHint RenderingHints/KEY_RENDERING
                         RenderingHints/VALUE_RENDER_QUALITY)
      (.setRenderingHint RenderingHints/KEY_ANTIALIASING
                         RenderingHints/VALUE_ANTIALIAS_ON))

    ;; Draw the original image, scaled to fit the new dimensions
    (.drawImage g original-image 0 0 new-width new-height nil)

    ;; Dispose of the graphics context to release system resources
    (.dispose g)

    ;; Return the newly created and drawn-upon image
    new-image))

(defn apply-watermark
  "Applies a watermark image onto a base image.

  The watermark is placed in the bottom-right corner with a 10-pixel padding.

  Parameters:
  - `base-image`: The `BufferedImage` to which the watermark will be applied.
    This image is modified in-place.
  - `watermark-image`: The `BufferedImage` to use as the watermark.

  Returns:
  - The modified `base-image` with the watermark drawn on it."
  [base-image watermark-image]
  (let [base-width (.getWidth base-image)
        base-height (.getHeight base-image)
        resized-watermark-image (resize-image watermark-image 0.2)
        mark-width (.getWidth resized-watermark-image)
        mark-height (.getHeight resized-watermark-image)
        padding 10
        ;; Calculate coordinates to place the watermark in the bottom-right
        x (- base-width mark-width padding)
        y (- base-height mark-height padding)
        ;; Get the Graphics2D object from the base image
        g (.createGraphics base-image)]

    ;; Draw the watermark image at the calculated position
    (.drawImage g resized-watermark-image x y nil)

    ;; Dispose of the graphics context
    (.dispose g)

    ;; Return the modified base image
    base-image))

(defn save-image
  "Saves a BufferedImage to a file on the local filesystem.

  The output format (e.g., \"png\", \"jpg\") is inferred from the file extension
  of the `output-path`.

  Parameters:
  - `image`: The `BufferedImage` to save.
  - `output-path`: A string representing the full path where the image will be saved
    (e.g., \"/path/to/output/result.png\").

  Returns:
  - `true` if the image was saved successfully, `false` otherwise."
  [image output-path]
  (let [format (-> output-path
                   (str/split #"\.")
                   last
                   str/lower-case)]
    (ImageIO/write image format (File. output-path))))

(defn process-image!
  "Orchestrates the entire image processing pipeline.

  This is the main function to call. It fetches an image from a URL, resizes it,
  applies a watermark, and saves the result.

  Parameters:
  - `image-url`: The URL of the source image.
  - `watermark-path`: The local file path to the watermark image (PNG or JPG).
  - `output-path`: The local file path where the final processed image will be saved.

  Returns:
  - `true` if the entire process completed successfully and the file was saved.
    An exception will be thrown if any step fails."
  [image-url watermark-path output-path]
  (println (str "Starting image processing for: " image-url))
  (let [;; 1. Fetch the main image from the URL
        original-img (fetch-image-from-url image-url)
        ;; 2. Resize the fetched image to 50% of its original size
        resized-img (resize-image original-img 0.5)
        ;; 3. Read the watermark image from the local file system
        watermark-img (read-image-from-file watermark-path)
        ;; 4. Apply the watermark onto the resized image
        final-img (apply-watermark resized-img watermark-img)
        ;; 5. Save the final image to the specified output path
        success (save-image final-img output-path)]
    (if success
      (println (str "Successfully processed and saved image to: " output-path))
      (println "Failed to save the image."))
    success))

;; --- USAGE EXAMPLE ---
;;
;; To use this namespace, you can call the `process-image!` function.
;; Make sure you have an image to watermark and a watermark image ready.
;;
(comment
  (def source-url "https://yulqen.org/img/poser_guy.png")
  (def my-watermark "/home/lemon/Pictures/RAW/2024-10-27-tyninghame/_build/static/leaflet/images/marker-icon-2x.png") ; <-- CHANGE THIS
  (def output-file "/tmp/toss.png") ; <-- CHANGE THIS

  ;; Run the entire process
  (process-image! source-url my-watermark output-file))
