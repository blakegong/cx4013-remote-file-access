import socket
import remoteFile
#socket setting
remoteFile.server_IP = '127.0.0.1'
remoteFile.server_PORT = 9800
client_PORT = 9900
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.bind(('', client_PORT))
sock.setblocking(0)
sock.settimeout(100)
#get input
while True:
	print '-----------------------'
	print 'Select Operation: 1: Read; 2: Insert; 3:Monitor; 4: Clear; 5:Exit'
	op = int(input('Operation? '))
	if op == 1:
		file = raw_input('File Name? ')
		offset = int(input('Offset? '))
		length = int(input('Length? '))
		semantic = int(input('Sem? 0.At least Once; 1.At Most Once\n'))
		data = remoteFile.read(sock, file, offset, length, semantic)
		print '[Read]','"',data,'"'
	elif op == 3:
		file = raw_input('File Name? ')
		dur = int(input('Interval (Seconds)? '))
		remoteFile.monitor(sock, file, dur)
	else:
		break
#close socket
sock.close()
