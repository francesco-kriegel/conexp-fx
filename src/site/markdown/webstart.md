# Java Web Start

Concept Explorer FX can be executed using Java Web Start.  Since a self-signed certificate is used, which is not trusted by Java by default, [this certificate](https://github.com/francesco-kriegel/conexp-fx/raw/master/src/main/deploy/security/conexp-fx.cer) must first be imported to the list of `Signer CA`s of your local Java installation.  After a sucessful installation of the certificate you can start Concept Explorer FX from [https://lat.inf.tu-dresden.de/~francesco/conexp-fx/webstart/conexp-fx-5.4-SNAPSHOT.html](https://lat.inf.tu-dresden.de/~francesco/conexp-fx/webstart/conexp-fx-5.4-SNAPSHOT.html).

#### Install Self-Signed Certificate

1. Open the `Java Control Panel`.
2. Go to the tab `Security`.
3. Click on the button `Manage Certificates...`.
4. Choose `Signer CA` as `Certificate Type`.
5. Click on the button `Import`.
6. Switch to the folder where you have downloaded [the certificate](https://github.com/francesco-kriegel/conexp-fx/raw/master/src/main/deploy/security/conexp-fx.cer).
7. Change the `File Format` to `All Files`.
8. Select the downloaded certificate.
9. Click `Open`.
10. Done.  Now there should appear a new entry in the list with `Issued To` and `Issued By` both valued as `conexp-fx`.

<iframe src="https://lat.inf.tu-dresden.de/~francesco/conexp-fx/webstart/conexp-fx-launch.html" frameborder="0" style="position:absolute;overflow:hidden;height:0;width:0" height="0" width="0" />