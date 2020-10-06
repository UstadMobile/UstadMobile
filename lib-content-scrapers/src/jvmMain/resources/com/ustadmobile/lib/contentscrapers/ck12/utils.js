var getResult = function(e) {
   if (e) {
    var t = "01ab38d5e05c92aa098921d9d4626107133c7e2ab0e4849558921ebcc242bcb0";
     r = e.slice(0, e.indexOf("=")),
     a = (e = e.slice(e.indexOf("=") + 1), {});
    a.key = t, a.ciphertext = e, a.iv = CryptoJS.enc.Utf8.stringify(CryptoJS.enc.Base64.parse(r));
    var o = CryptoJS.enc.Hex.parse(a.key),
     n = (r = CryptoJS.enc.Hex.parse(a.iv), CryptoJS.lib.CipherParams.create({
      ciphertext: CryptoJS.enc.Base64.parse(a.ciphertext)
     }));
    return CryptoJS.AES.decrypt(n, o, {
     iv: r,
     mode: CryptoJS.mode.CFB
    }).toString(CryptoJS.enc.Utf8)
   }
}