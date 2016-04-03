import util
import datetime
import socket

server_IP = '192.168.0.150'
server_PORT = 9800
FRAMESIZE = 1024
BUFFERSIZE = FRAMESIZE + 64

def read(serverSock, file, offset, length, semantic):
	#check in cache?
	operation = {'op':0, 'f': file, 'off':offset, 'len':length, 'sem':semantic}
	operation = util.marshall(operation);
	while True:
		serverSock.sendto(operation, (server_IP, server_PORT))
		data, address = serverSock.recvfrom(BUFFERSIZE)
		if len(data) != 0:
			break
	response = util.unmarshall(data)
	if 'Exception' in response:
		print 'Exception ', response['Exception']
		return
	#put in cache?
	if 'data' in response:
		index = offset - response['off']
		remain = response['len'] - index
		if length <= remain:
			return response['data'][index:index+length]
		else:
			return response['data'][index:index+remain]
	return

def monitor(serverSock, file, interval):
	operation = {'op':2, 'f': 'readme.txt', 'dur':interval, 'sem':0}
	operation = util.marshall(operation);
	startTime = datetime.datetime.now();
	while True:
		serverSock.sendto(operation, (server_IP, server_PORT))
		try:
			data = serverSock.recv(BUFFERSIZE)
			if len(data) != 0:
				break
		except socket.timeout:
			print 'Resending command'
	response = util.unmarshall(data)
	print (response)
	if 'Exception' in response:
		print 'Exception ', response['Exception']
		return
	print 'Entering Monitor Mode...'
	while True:
		try:
			data = serverSock.recv(BUFFERSIZE)
			response = util.unmarshall(data)
			print response
			if 'Expired' in response:
				print 'End of Monitoring...'
				break
			deltaTime = datetime.datetime.now() - startTime;
			#if deltaTime.seconds > data['dur']:
				#break
		except socket.timeout:
			print '[Waiting for data]'
	return
#send operations
#operation = {'op':1, 'off':41, 'f': 'readme.txt', 'data': '[CE4013]', 'sem':0}
#operation = {'op':3, 'f':'readme.txt'}
