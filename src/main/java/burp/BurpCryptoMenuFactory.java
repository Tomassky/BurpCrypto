package burp;

import crypto.*;
import utils.BurpConfig;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BurpCryptoMenuFactory implements IContextMenuFactory{

    private final BurpConfig burpConfig;

    public BurpCryptoMenuFactory() {
        this.burpConfig = new BurpConfig();
    }

    @Override
    public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
        List<JMenuItem> menus = new ArrayList<>();
        menus.add(createMenuItem("Get PlainText", "decrypt", invocation));
        menus.add(createMenuItem("Quick Crypto", "encrypt", invocation));
        return menus;
    }

    private JMenuItem createMenuItem(String title, String method, IContextMenuInvocation invocation) {
        JMenuItem menuItem = new JMenuItem(title);
        menuItem.addActionListener(e -> processCryptoAction(invocation, method));
        return menuItem;
    }

    private void processCryptoAction(IContextMenuInvocation invocation, String method) {
        IHttpRequestResponse[] iHttpRequestResponses = invocation.getSelectedMessages();
        if (iHttpRequestResponses.length > 0) {
            IHttpRequestResponse iHttpRequestResponse = iHttpRequestResponses[0];
            byte[] request = iHttpRequestResponse.getRequest();
            String selectedText = getSelectedText(request, invocation.getSelectionBounds());
            if (selectedText != null && !selectedText.isEmpty()) {
                String resultText = handleCryptoDispatch(selectedText, method);
                if (resultText != null && !resultText.isEmpty()) {
                    ShowCopiableMessage(resultText, "This message " + method + " is: ");
                    //iHttpRequestResponse.setComment(resultText);
                } else {
                    JOptionPane.showMessageDialog(null, "Not found!");
                }
            }
        }
    }

    private String handleCryptoDispatch(String selectText, String method) {
        String cryptoRequestMethod = burpConfig.getProperty("cryptoRequestMethod");
        String cryptoResponseMethod = burpConfig.getProperty("cryptoResponseMethod");
        IParamCrypto iParamCrypto = new ParamCrypto();

        if ("encrypt".equals(method)) {
            iParamCrypto = configureCryptoWrapper(iParamCrypto, cryptoRequestMethod);
            return iParamCrypto.encryptParam(selectText);
        } else if ("decrypt".equals(method)) {
            iParamCrypto = configureCryptoWrapper(iParamCrypto, cryptoResponseMethod);
            return iParamCrypto.decryptParam(selectText);
        }
        return null;
    }

    private IParamCrypto configureCryptoWrapper(IParamCrypto iParamCrypto, String method) {
        switch (method) {
            case "Base64":
                return new Base64ParamCryptoWrapper(iParamCrypto);
            case "AES":
                return new AESParamCryptoWrapper(iParamCrypto);
            case "Sekiro":
                return new SekiroParamCryptoWrapper(iParamCrypto);
            case "MD5":
                return new MD5ParamCryptoWrapper(iParamCrypto);
            case "JSEngine":
                return new JSEngineParamCryptoWrapper(iParamCrypto);
            default:
                return iParamCrypto;
        }
    }



    private String getSelectedText(byte[] request, int[] selectedIndexRange) {
        try {
            return new String(Objects.requireNonNull(getSelectedBytes(request, selectedIndexRange)));
        } catch (Exception ex) {
            return null;
        }
    }

    private byte[] getSelectedBytes(byte[] request, int[] selectedIndexRange) {
        try {
            byte[] selectedText = new byte[selectedIndexRange[1] - selectedIndexRange[0]];
            System.arraycopy(request, selectedIndexRange[0], selectedText, 0, selectedText.length);
            return selectedText;
        } catch (Exception ex) {
            return null;
        }
    }



    public void ShowCopiableMessage(String message, String title) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JTextArea ta = new JTextArea(5, 20);
                ta.setText(message);
                ta.setWrapStyleWord(true);
                ta.setLineWrap(true);
                ta.setCaretPosition(0);
                ta.setEditable(false);
                JOptionPane.showMessageDialog(null, new JScrollPane(ta), title, JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }
}
