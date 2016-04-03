"""
Client side request and reply protocol.
"""

import socket
from datetime import datetime

class ProtocolLayer():
    """
    Implementation of protocols on client side.
    """
    def __init__(self):
        self.READ_OP = 0
        self.INSERT_OP = 1
        self.MONITOR_OP = 2
        self.CLEAR_OP = 3
        self.DELETE_OP = 4
        self.BYTE_ORDER = 'big'
        self.SERVER_IP = input("Enter Server's IP Address: ")
        self.SERVER_PORT = int(input("Enter Server's Port Number: "))
        self.SOCKET = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.SOCKET.settimeout(3)
        self.FRAMESIZE = 1024

    def _marshall(self, request):
        """
        Marshall the request dict into a bytearray.
        """
        msg = len(request).to_bytes(4, byteorder=self.BYTE_ORDER)
        for key in request:
            value = request[key]
            # Only bytes and int can be marshalled
            if type(value) == bytes:
                value = b'r' + value + b'\0'
            elif type(value) == int:
                value = b'i' + value.to_bytes(4, byteorder=self.BYTE_ORDER)
            else:
                raise Exception('Marshalling unrecognized data type.')
            # Construct message
            msg += str.encode(key) + b'\0:' + value
        return msg

    def _unmarshall(self, reply):
        """
        Unmarshall the reply bytes into a dict.
        """
        msg = dict()
        msg_num = int.from_bytes(reply[:4], byteorder=self.BYTE_ORDER)
        msg_length = len(reply)
        _s = 4
        while _s < msg_length:
            # Get key until b'\0'
            _e = _s
            while reply[_e:_e + 1] != b'\0':
                _e += 1
            key = bytes.decode(reply[_s:_e])
            # Get the ':'
            _s = _e + 1
            if reply[_s:_s + 1] != b':':
                raise Exception('Unmarshalling expect a ":" after key, get {} instead.'.format(reply[_s:_s+1]))
            # Get type and the value
            _s += 1
            if reply[_s:_s + 1] == b'r':
                # Type: bytes
                _s += 1
                _e = _s
                while reply[_e:_e + 1] != b'\0':
                    _e += 1
                value = reply[_s:_e]
                _s = _e + 1
            elif reply[_s:_s+1] == b'i':
                # Type: int
                _s += 1
                value = int.from_bytes(reply[_s:_s + 4], byteorder=self.BYTE_ORDER)
                _s += 4
            else:
                raise Exception('Unmarshalling unrecognized data type.')
            msg[key] = value
        return msg

    def _send_udp(self, request):
        request = self._marshall(request)
        times = 1
        while True:
            self.SOCKET.sendto(request, (self.SERVER_IP, self.SERVER_PORT))
            try:
                msg = self.SOCKET.recv(self.FRAMESIZE)
                if len(msg) != 0:
                    break
            except socket.timeout:
                print('TIMEOUT - No. of resend: {}'.format(times))
                times += 1
        return self._unmarshall(msg)

    def READ(self, pathname, offset, length):
        """
        Implementation of READ protocol.
        """
        request = {
            'op': self.READ_OP,
            'f': str.encode(pathname),
            'off': offset,
            'len': length,
            'time': str.encode(str(datetime.now()))
        }
        msg = self._send_udp(request)
        return msg

    def INSERT(self, pathname, offset, content):
        """
        Implementation of INSERT protocol.
        """
        request = {
            'op': self.INSERT_OP,
            'f': str.encode(pathname),
            'off': offset,
            'data': content,
            'time': str.encode(str(datetime.now()))
        }
        msg = self._send_udp(request)
        return msg

    def MONITOR(self, pathname):
        """
        Implementation of MONITOR protocol.
        """
        pass

    def CLEAR(self, pathname):
        """
        Implementation of CLEAR protocol.
        """
        request = {
            'op': self.CLEAR_OP,
            'f': str.encode(pathname),
            'time': str.encode(str(datetime.now()))
        }
        msg = self._send_udp(request)
        return msg

    def DELETE(self, pathname, offset, length):
        """
        Implementation of DELETE protocol.
        """
        request = {
            'op': self.DELETE_OP,
            'f': str.encode(pathname),
            'off': offset,
            'len': length,
            'time': str.encode(str(datetime.now()))
        }
        msg = self._send_udp(request)
        return msg
