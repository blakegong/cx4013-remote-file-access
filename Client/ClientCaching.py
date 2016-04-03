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
        self.version = ''

    def is_alive(self):
        return (datetime.now() - self.last_update).total_seconds() < self.ttl

    def get(self, offset, length):
        """
        Get content of cache entry. Throw exceptions if necessary.
        """
        return b"".join(self.content[i] for i in range(offset, offset + length))

    def set(self, offset, content, version):
        """
        Set content of cache entry.
        """
        for i in range(len(content)):
            self.content[i + offset] = content[i:i+1]
        self.last_update = datetime.now()
        self.version = version

    def reset(self):
        """
        Reset content of cache entry.
        """
        self.content = dict()
        self.version = ''

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
            if not self.entries[pathname].is_alive():
                print('\tCache entry is dead, better check file version')
                if self.PROTOCOL.VERSION(pathname)['version'] != self.entries[pathname].version:
                    print('\tCache is outdated from server')
                    raise Exception('Cache is outdated from server')
                else:
                    print('\tCache is up-to-date with server')
            else:
                print('\tCache entry is alive')
            print('\tData from cache:', cached)
        except:
            msg = self.PROTOCOL.READ(pathname, offset, length)
            if 'Exception' in msg:
                print('\tError Message:', bytes.decode(msg['Exception']))
            elif 'data' in msg and 'version' in msg:
                data = msg['data']
                version = msg['version']
                print('\tData from Server:', data)
                if pathname in self.entries:
                    self.entries[pathname].reset()
                    self.entries[pathname].set(offset, data, version)
                else:
                    entry = CacheEntry(pathname, self.ttl)
                    entry.set(offset, data, version)
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
                self.entries[pathname].reset()
                print('\t\tCache entry has been reset')
            except:
                pass

    def MONITOR(self, pathname, dur):
        self.PROTOCOL.MONITOR(pathname, dur)
        try:
            self.entries[pathname].reset()
            print('\t\tCache entry has been reset')
        except:
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
                self.entries[pathname].reset()
                print('\t\tCache entry has been reset')
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
                self.entries[pathname].reset()
                print('\t\tCache entry has been reset')
            except:
                pass
