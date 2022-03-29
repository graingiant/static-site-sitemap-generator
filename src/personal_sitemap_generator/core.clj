(ns personal-sitemap-generator.core
  (:require  [clojure.java.io :as io]
             [clojure.string :as str]
             [sitemap.core :as sitemap]
             [clojure.tools.cli :refer [parse-opts]])

  (:import (java.time LocalDate))
  (:gen-class))

(def date-formatted (.toString (LocalDate/now)))

(defn get-directory-listing [dir-path]
  (map #(.getPath %) (file-seq (io/file dir-path))))

(defn list-middle [path-list]
  (rest (drop-last path-list)))

(defn get-root-path-for-url [root-file path]
  (when (str/includes? path root-file)
    (->> (str/split path #"/")
         (list-middle)
         (str/join "/"))))

(defn to-sitemap-hash [root-url item]
  {:loc (str root-url item)
   :lastmod date-formatted})

(defn root-pages [dir-path root-url root-file]
  (->> (get-directory-listing dir-path)
       (map (partial get-root-path-for-url root-file))
       (filter not-empty)
       (map (partial to-sitemap-hash root-url))))

(def cli-options [[nil "--path PATH" "Directory path to scan"]
                  [nil "--url ROOT_URL" "The root URL of the sitemap to generate. Trailing slash required! Example: https://example.com/"]
                  [nil "--root-file FILE_NAME" "NOT IMPLEMENTED: index.html or index.php like file to consider the 'root file' for each directory. Will skip any directories that don't contain this file, and will only use directories with this file to build sitemap" :id :filename :default "index.html"]])

(defn -main
  [& args]
  (let [opts (parse-opts args cli-options)
        path (get-in opts [:options :path])
        url (get-in opts [:options :url])
        root-file (get-in opts [:options :filename])]
    (spit (str path "/sitemap.xml") (sitemap/generate-sitemap (root-pages path url root-file)))))
