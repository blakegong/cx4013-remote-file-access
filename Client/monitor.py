import socket
import util
#socket setting
server_IP = '127.0.0.1'
server_PORT = 9800
client_PORT = 10001
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.bind(('127.0.0.1', client_PORT))
sock.settimeout(10)
#send operations
#'time': timestamp
#operation = {'op':0, 'f': 'readme.txt', 'off':2, 'len':10, 'sem':0, 't': 'time'}#read
operation = {'op':1, 'off':41, 'f': 'readme.txt', 'data': '[CE4013]', 'sem':0, 't':'time'}#insert
#operation = {'op':2, 'f': 'readme.txt', 'dur':30, 'sem':0, 't':'time'}#register
#operation = {'op':3, 'f':'readme.txt', 't':'time'}#Clear
#operation = {'op':4, 'f': 'readme.txt', 'off':2, 'len':3}

#At least once -> nothing
#At most once -> history <"IP":String>
operation = util.marshall(operation);
sock.sendto(operation, (server_IP, server_PORT))

#receive data
data, address = sock.recvfrom(1024)
response = util.unmarshall(data)
print response
sock.close()
