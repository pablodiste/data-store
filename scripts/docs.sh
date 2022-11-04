cp README.md ../data-store-site/docs/datastore.md
cd ../data-store-site
mkdocs build
cp -R site/* ../data-store-site-public/
cd ../data-store-site-public/
git add *
git commit -m "Update"
git push
