<h1 align="center">BurpCrypto</h1>

<p align="center">
🔥 The Burpsuite Extension for auto encrypt an decrypt
</p>

## Fifth mode

- The Struts for the data：use wrapper class to parse the data(body+header+url)
  - json
  - form
  - string(all)
  - xml

- Encrypted Sign
  - all body
  - Single sign(based on headers)
  - single sign(based on body)

- Request and Response package
  - entire request body crypto
  - some params cryptoed in request body
  - entire response body
  - some params crypro in response body

- Crypto class:
  - base64
  - aes
  - md5
  - JSEngine
  - sekiro

- The encryption client: 
  - web
  - miniprogram
  - app

- Proxy -> Qucik Crypto/Get PlainText
- Repeater -> Auto Crypto
- Mind Mapping
  - Burpsuite_Sekiro.xmind(About the Serkiro in Burpsuite and the processProxyMessage()/processHttpMessage())

## Or you can look for the DEVELOP.md file