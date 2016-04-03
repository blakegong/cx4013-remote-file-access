"""
Implementation of client-side caching in memory.
"""

from datetime import datetime

from ClientProtocol import ProtocolLayer

class CacheEntry():
    """
    An entry of a cached file.
    """
    def __init__(self, pathname, ttl):
        self.pathname = pathname
        self.ttl = ttl
        self.content = dict()
        self.last_update = datetime.now()

    def get(self, offset, length):
        """
        Get content of cache entry. Throw exceptions if necessary.
        """
        if (datetime.now() - self.last_update).total_seconds() > self.ttl:
            raise Exception('Cache outdated. ')
        return b"".join(self.content[i] for i in range(offset, offset + length))

    def set(self, offset, content):
        """
        Set content of cache entry.
        """
        for i in range(len(content)):
            self.content[i + offset] = content[i:i+1]
        self.last_update = datetime.now()

class CachedLayer():
    """
    A cache layer for remote file access.
    """
    def __init__(self, ttl=10):
        self.entries = dict()
        self.PROTOCOL = ProtocolLayer()
        self.ttl = ttl

    def READ(self, pathname, offset, length):
        """
        Caching for READ protocol.
        """
        try:
            cached = self.entries[pathname].get(offset, length)
            print('\tData from Cache:', cached)
        except:
            msg = self.PROTOCOL.READ(pathname, offset, length)
            if 'Exception' in msg:
                print('\tError Message:', bytes.decode(msg['Exception']))
            elif 'data' in msg:
                data = msg['data']
                print('\tData from Server:', data)
                if pathname in self.entries:
                    self.entries[pathname].set(offset, data)
                else:
                    entry = CacheEntry(pathname, self.ttl)
                    entry.set(offset, data)
                    self.entries[pathname] = entry
                print('\t\tWritten to cache, which expires after {} seconds'.format(self.ttl))

    def INSERT(self, pathname, offset, content):
        """
        Caching for INSERT protocol.
        """
        msg = self.PROTOCOL.INSERT(pathname, offset, content)
        if 'Exception' in msg:
            print('\tError Message:', bytes.decode(msg['Exception']))
        elif 'ACK' in msg:
            print('\tACK:', bytes.decode(msg['ACK']))
            try:
                del self.entries[pathname]
                print('\t\tCache entry becomes invalid')
            except:
                pass

    def MONITOR(self, pathname):
        pass

    def CLEAR(self, pathname):
        """
        Caching for CLEAR protocol.
        """
        msg = self.PROTOCOL.CLEAR(pathname)
        if 'Exception' in msg:
            print('\tError Message:', bytes.decode(msg['Exception']))
        elif 'ACK' in msg:
            print('\tACK:', bytes.decode(msg['ACK']))
            try:
                del self.entries[pathname]
                print('\t\tCache entry becomes invalid')
            except:
                pass

    def DELETE(self, pathname, offset, content):
        """
        Caching for DELETE protocol.
        """
        msg = self.PROTOCOL.DELETE(pathname, offset, content)
        if 'Exception' in msg:
            print('\tError Message:', bytes.decode(msg['Exception']))
        elif 'ACK' in msg:
            print('\tACK:', bytes.decode(msg['ACK']))
            try:
                del self.entries[pathname]
                print('\t\tCache entry becomes invalid')
            except:
                pass

if __name__ == '__main__':
    cl = CachedLayer(20)
    cl.READ('readme.txt', 0, 5)
    cl.READ('readme.txt', 0, 3)
    cl.INSERT('readme.txt', 0, b'xxx')
    cl.READ('readme.txt', 0, 2)
