(ns personal-sitemap-generator.core
  (:require  [clojure.java.io :as io]
             [clojure.string :as str]
             [sitemap.core :as sitemap]
             [clojure.tools.cli :refer [parse-opts]]
             [clojure.pprint :refer [pprint]])

  (:import (java.time LocalDate))
  (:gen-class))

(def date-formatted (.toString (LocalDate/now)))

(defn get-directory-listing [dir-path]
  (map #(.getPath %) (file-seq (io/file dir-path))))

(defn list-middle [path-list]
  (rest (drop-last path-list)))

(defn get-root-path-for-url [path]
  (when (str/includes? path "index.html")
    (->> (str/split path #"/")
         (list-middle)
         (str/join "/"))))

(defn to-sitemap-hash [item]
  {:loc (str "https://alexcaza.com/" item)
   :lastmod date-formatted})

(defn root-pages [dir-path]
  (->> (get-directory-listing dir-path)
       (map get-root-path-for-url)
       (filter not-empty)
       (map to-sitemap-hash)))

(def cli-options [["-p" "--path PATH" "Directory path to scan"]
                  ;; TODO Support -r option
                  ["-r" "--root-file" "NOT IMPLEMENTED: index.html or index.php like file to consider the 'root file' for each directory. Will skip any directories that don't contain this file, and will only use directories with this file to build sitemap" :id :filename]])

(defn -main
  [& args]
  (let [opts (parse-opts args cli-options)
        path (get-in opts [:options :path])]
    (pprint opts)
    (pprint path)
    (spit (str path "/sitemap.xml") (sitemap/generate-sitemap (root-pages path)))))
