package ru.seits.simplebackup;


class FileAttr {
	private String path;
	private long dateChang;
	private long size;
	private String hash;
	
	public FileAttr() {
		this.path = "";
		this.dateChang = 0;
		this.size = -1;
		this.hash = "";
	}
	
	public FileAttr(String path, long dateChang, long size, String hash) {
		this.path = path;
		this.dateChang = dateChang;
		this.size = size;
		this.hash = hash;
	}

	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

	public long getDateChang() {
		return dateChang;
	}
	public void setDateChang(long dateChang) {
		this.dateChang = dateChang;
	}

	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}

	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}

	@Override
	public String toString() {
		return "FileAttr [path=" + path + ", dateChang=" + dateChang
				+ ", size=" + size + ", hash=" + hash + "]";
	}
	
	
}
