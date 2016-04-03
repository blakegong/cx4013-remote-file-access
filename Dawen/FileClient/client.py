import socket
import util
#socket setting
server_IP = '10.27.126.227'
server_PORT = 9800
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

#send operations
operation = {'op':0, 'off':1, 'len':5, 'f': 'readme.txt'}
operation = util.marshall(operation);
sock.sendto(operation, (server_IP, server_PORT))

#receive data
data, address = sock.recvfrom(1024)
response = util.unmarshall(data)
print(response)
sock.close()
