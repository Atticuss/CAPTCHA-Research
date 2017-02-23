import requests, sqlite3, time
from bs4 import BeautifulSoup
from requests.packages.urllib3.exceptions import InsecureRequestWarning
requests.packages.urllib3.disable_warnings(InsecureRequestWarning)

proxyUrls = ['http://www.sslproxies.org/', 'http://www.us-proxy.org/', 'http://free-proxy-list.net/uk-proxy.html']
db = './corpus.sqlite'
conn = sqlite3.connect(db)

def totalProxies(conn):
    curs = conn.cursor()
    curs.execute('select * from proxies')
    return len(curs.fetchall())

while(True):
    for proxyUrl in proxyUrls:
        r = requests.get(proxyUrl)
        soup = BeautifulSoup(r.text, 'html.parser')
        goodProxies = 0

        for row in soup.find_all('tr')[1:21]:
            col = row.find_all('td')
            ip = col[0].string.strip()
            port = col[1].string.strip()
            proxy = '%s:%s' % (ip, port)

            proxy_dict = {
                'http' : proxy,
                'https' : proxy
            }

            try:
                #print('trying proxy: %s' % proxy)
                r = requests.get('https://www.service.com', proxies=proxy_dict, verify=False, timeout=5)
                curs = conn.cursor()
                curs.execute('select * from proxies where proxy=?', (proxy,))

                if len(curs.fetchall()) == 0:
                    print('[*] Proxy found: %s' % proxy)
                    curs.execute('insert into proxies (proxy) values (?)', (proxy,))
                    conn.commit()
                    goodProxies += 1
                    #print('[*] valid proxy found')
                else:
                    #print('[!] proxy already known')
                    pass
            except:
                #print('[!] invalid proxy')
                pass

        print('[*] %s new proxies found' % goodProxies)
    print('[*] Total proxies: %s' % totalProxies(conn))
    time.sleep(600)