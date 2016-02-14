cd src/main/resources/conexp/fx/gui/image
mkdir conexp-fx.iconset
sips -z 16 16     conexp-fx.png --out conexp-fx.iconset/icon_16x16.png
sips -z 32 32     conexp-fx.png --out conexp-fx.iconset/icon_16x16@2x.png
sips -z 32 32     conexp-fx.png --out conexp-fx.iconset/icon_32x32.png
sips -z 64 64     conexp-fx.png --out conexp-fx.iconset/icon_32x32@2x.png
sips -z 64 64     conexp-fx.png --out conexp-fx.iconset/icon_64x64.png
sips -z 128 128   conexp-fx.png --out conexp-fx.iconset/icon_64x64@2x.png
sips -z 128 128   conexp-fx.png --out conexp-fx.iconset/icon_128x128.png
cp conexp-fx.png conexp-fx.iconset/icon_128x128@2x.png
cp conexp-fx.png conexp-fx.iconset/icon_256x256.png
iconutil -c icns conexp-fx.iconset
rm -R conexp-fx.iconset
mv conexp-fx.icns ../../../../../../../src/main/deploy/package/macosx