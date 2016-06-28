package com.wusiv;

/*
* Arduino Logger
*
* Created on: 26.FEB.2016
* Last Updated on: 26.MAR.2016
* Author: Yusuf KALSEN
*
*
*
*
*
* Arduino logger is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published
* by the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Arduino logger is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with Arduino logger. If not, see <http://www.gnu.org/licenses/>.
 */
import com.fazecast.jSerialComm.SerialPort;   /// http://fazecast.github.io/jSerialComm/

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

public class JArduinoLog {

    static SerialPort Port; //serial port
    static String dt = null, tmpDate = null; //date

    //gui
    static JFrame win; //window
    static JLabel lblCom;
    static JComboBox<String> cmbPort;
    static JButton btnConnect;
    static JTextArea txtAreaLog;
    static JButton btnSave;
    static JButton btnClear;
    static JScrollPane bar;       // scroll for JTextArea
    static DefaultCaret cr; //auto scroll JtextArea (otomatik

    
    /**
     * Count Port
     */
    static void portCount() {
        if (cmbPort.getItemCount() < 1) {
            btnConnect.enable(false);
        } else {
            btnConnect.enable(true);
            cmbPort.removeAllItems();
        }
    }

    /**
     * comPort()
     *
     * add ports to combobox
     *
     */
    static void comPort() {
        portCount();
        SerialPort[] ports = SerialPort.getCommPorts();

        for (int i = 0; i < ports.length; i++) {  // list all COM ports (tüm COM iletişim portlarini ekleyecek)
            cmbPort.addItem(ports[i].getSystemPortName());

        }

    }

    /**
     * save to file (dosyaya kayıt)
     *
     * @param FileName
     * @param input
     * @param date
     * @return get file full path
     */
    static String fWrite(String FileName, String input, String date) {

        String tmpPath = (new File("").getAbsolutePath() + "\\" + FileName + "-" + date + ".txt");
        try {

            BufferedWriter logBw = new BufferedWriter(new FileWriter(tmpPath, true));

            logBw.append(input);
            logBw.flush();
            logBw.close();

        } catch (IOException IOE1) {
            System.out.println("IO EXCEPTION 1: " + IOE1.toString());
        }
        return tmpPath;
    }

    /**
     * time Update zaman Güncelemesi icin void
     */
    static void upTime() {

        LocalTime hr = LocalTime.now();
        LocalDate dy = LocalDate.now();

        dt = dy.toString() + "," + hr.getHour() + ":" + hr.getMinute() + ":" + hr.getSecond();
        ;
        tmpDate = dy.toString();
    }

    /**
     * GUI
     */
    static void gui() {

        /// Controls
        win = new JFrame();
        lblCom = new JLabel("Port");
        cmbPort = new JComboBox<String>();
        btnConnect = new JButton("Bağlan");
        txtAreaLog = new JTextArea("");
        btnSave = new JButton("Kaydet...");
        btnClear = new JButton("Temizle");
        bar = new JScrollPane(txtAreaLog);       // scroll for JTextArea
        cr = (DefaultCaret) txtAreaLog.getCaret(); //auto scroll JtextArea (otomatik

        win.setSize(500, 400);
        win.setResizable(false);
        win.setLayout(null);
        win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        win.setTitle("Arduino Logger");
        win.setLocationRelativeTo(null);

        lblCom.setLocation(20, 25);
        lblCom.setSize(35, 15);

        cmbPort.setLocation(50, 20);
        cmbPort.setSize(80, 25);

        btnConnect.setLocation(142, 20);
        btnConnect.setSize(80, 25);

        btnClear.setLocation(250, 20);
        btnClear.setSize(80, 25);

        btnSave.setLocation(380, 20);
        btnSave.setSize(100, 25);
        btnSave.setEnabled(false);

        bar.setLocation(10, 70 - 10 - 10);
        bar.setSize(460 + 10, 220 + 30 + 50 + 10);

        cr.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); //every time focus to last Line (her zaman son satira odaklanacak)

        txtAreaLog.setEditable(false);

        win.add(lblCom);
        win.add(cmbPort);
        win.add(btnConnect);
        win.add(btnSave);
        win.add(btnClear);

        win.add(bar);

        Timer t = new Timer(0, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                upTime();
                comPort();

            }
        });
        t.start();

        /**
         * connect button function
         *
         */
        btnConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btnConnect.getText().equals("Bağlan")) {
                    Port = SerialPort.getCommPort(cmbPort.getSelectedItem().toString());
                    Port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
                    System.out.println(Port.getBaudRate());
                    if (!Port.isOpen()) {
                        Port.openPort();
                        if (Port.openPort()) {
                            btnConnect.setText("Kes");
                            cmbPort.setEnabled(false);
                            btnSave.setEnabled(false);

                        }

                        Thread th = new Thread() {

                            @Override

                            public void run() {

                                Scanner scan = new Scanner(Port.getInputStream());
                                while (scan.hasNextLine()) {

                                    txtAreaLog.append(dt + "," + scan.nextLine() + "\n");
                                }
                            }
                        };
                        th.start();
                    } else {
                        JOptionPane.showMessageDialog(null, "Com Port Açık veya kullanılıyor");
                    }

                } else {
                    Port.closePort();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    btnConnect.setText("Bağlan");
                    cmbPort.setEnabled(true);
                    if ((txtAreaLog.getText().length() < 1)) {
                        btnSave.setEnabled(false);
                        btnClear.setEnabled(false);
                    } else {
                        btnSave.setEnabled(true);
                        btnClear.setEnabled(true);
                    }

                }
            }

        });

        /**
         * save button function
         */
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (!(txtAreaLog.getText().length() < 1)) {
                    String file = JOptionPane.showInputDialog("Dosya ismi ");
                    if (file != null) {
                        String msg = fWrite(file, txtAreaLog.getText(), tmpDate);

                        txtAreaLog.setText(null);
                        txtAreaLog.append("\nDosya kayıt yeri: \n" + msg + "\n");
                    }
                }

            }
        });

        /**
         * Clear Button function
         *
         */
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtAreaLog.setText(null);
                btnSave.setEnabled(false);
            }
        });

        win.setVisible(true);
    }

    public static void main(String args[]) {

        gui();

    }

}
