import pyautogui
import ftplib
import time
from pynput.mouse import Button, Controller
import urllib.request
mouse=Controller()

def screen_up_lite():
    Screen= pyautogui.screenshot()
    Screen.save('desktop.png')
    #dodac kursor do apki
    filename='desktop.png'
    ftp=ftplib.FTP('files.000webhost.com')
    ftp.login('zalcizenie','Diablica2')
    ftp.cwd("public_html")

    myfile = open('desktop.png','rb')
    ftp.storbinary('STOR desktop.png', myfile)
    myfile.close()
    ftp.quit()
    
def screen_and_up():
    Screen= pyautogui.screenshot()
    Screen.save('desktop.png')
    
    filename='desktop.png'
    ftp=ftplib.FTP('files.000webhost.com')
    ftp.login('zalcizenie','Diablica2')
    ftp.cwd("public_html")
    
    myfile = open('desktop.png','rb')
    ftp.storbinary('STOR desktop.png', myfile)
    myfile.close()
    myfile=open('request_data.txt','rb')
    ftp.storbinary('STOR request_data.txt', myfile)
    myfile.close()
    ftp.quit()

def download_file():
    filename='request_data.txt'
    ftp=ftplib.FTP('files.000webhost.com')
    ftp.login('zalcizenie','Diablica2')
    ftp.cwd("public_html")
    
    tablica=[]
    ftp.retrlines('RETR '+ filename, tablica.append)
    
    plik=open(filename,'w')
    plik.write(tablica[0])
    ftp.quit()
    plik.close()

def przeczytaj_html():
    response = urllib.request.urlopen('https://zalcizenie.000webhostapp.com')
    html = response.read()
    html=str(html)
    html=html.split('SLICER')
    html=str(html[1])
    html=html.replace(r'\r\n','\n')
    print(html)

def main():
    download_file()
    plik=open('request_data.txt',"r")
    wykonane='skrindano'
    plik_str=plik.read()
    tab_operacji=[]
    if plik_str!=wykonane:
        tab_operacji=plik_str.split(' ')
        plik.close()
        
        mouse.position=(tab_operacji[0],tab_operacji[1])
        if tab_operacji[2]=='klik':
            mouse.press(Button.left)
            mouse.release(Button.left)
        elif tab_operacji[2]=='doubleklik':
            mouse.press(Button.left)
            mouse.release(Button.left)
            time.sleep(1/10)
            mouse.press(Button.left)
            mouse.release(Button.left)
        elif tab_operacji[2]=='trzymajl':
            mouse.press(Button.left)
        elif tab_operacji[2]=='puscl':
            mouse.release(Button.left)
        elif tab_operacji[2]=='klikp':
            mouse.press(Button.right)
            mouse.release(Button.right)

        plik=open('request_data.txt','w')
        plik.write(wykonane)
        plik.close()
        screen_and_up()

while True:
    time.sleep(2)
    main()
    
