package com.vtd.pianoapp.util;

import android.content.Context;

import java.io.*;


public class FileUtils {
	
	/**
	 * Create new file to folder with fileName
	 * @param data : string to write into file
	 * @param folder : path of file
	 * @param fileName : name of file
	 */
	
	public static void writeFile(String data,String folder,String fileName){
		if(data ==null){
			return;
		}
		File fileFolder = new File(folder);
		if(!fileFolder.exists()){
			fileFolder.mkdirs();
		}
		
		File file = new File(folder,fileName);
		try {
			FileOutputStream fOut = new FileOutputStream(file);
			OutputStreamWriter oWriter = new OutputStreamWriter(fOut);
			oWriter.write(data);
			oWriter.flush();
			oWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Read file to string
	 * @param folder path of file
	 * @param fileName name of file
	 * @return string if file exit, null if file not exit
	 */
	public static String readFile(String folder,String fileName){
		
		File file = new File(folder,fileName);
		if(file.exists()){
			FileInputStream fIn;
			try {
				fIn = new FileInputStream(file);
			Writer writer = new StringWriter();
			char[] buffer = new char[1024];
			Reader reader;
				reader = new BufferedReader(new InputStreamReader(fIn,"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
				fIn.close();
				return writer.toString();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	

	public static String readFile(String filePath){
		
		File file = new File(filePath);
		if(file.exists()){
			FileInputStream fIn;
			try {
				fIn = new FileInputStream(file);
			Writer writer = new StringWriter();
			char[] buffer = new char[1024];
			Reader reader;
				reader = new BufferedReader(new InputStreamReader(fIn,"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
				fIn.close();
				return writer.toString();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * copy file from Src to DSt and can rename
	 * 
	 * @param mSrc
	 * @param mDst
	 * @param mNameDst
	 */
	public static boolean copyFile(File mSrc, File mDst, String mNameDst) {
		if (!mSrc.exists())
			return false;
		try {
			String mPath = mDst.getAbsolutePath();
			if (mSrc.isDirectory()) {
				File[] listFiles = mSrc.listFiles();
				int lenght = listFiles.length;

				File mFolder;
				if (mNameDst != null) {
					mFolder = new File(mPath + "/" + mNameDst);
				} else {
					mFolder = new File(mPath + "/" + mSrc.getName());
				}
				if (!mFolder.exists()) {
					mFolder.mkdirs();
				}
				for (int i = 0; i < lenght; i++) {
					File mFile = listFiles[i];
					FileInputStream mFileInputStream = new FileInputStream(mFile);
					FileOutputStream mFileOutputStream = new FileOutputStream(mFolder.getAbsolutePath() + "/" + mFile.getName());
					byte[] buffer = new byte[1024];
					int len1 = 0;
					while ((len1 = mFileInputStream.read(buffer)) > 0) {
						mFileOutputStream.write(buffer, 0, len1);
					}
					mFileOutputStream.flush();
					mFileOutputStream.close();
				}
			} else {
				FileInputStream mFileInputStream = new FileInputStream(mSrc);
				FileOutputStream mFileOutputStream = new FileOutputStream(mDst.getAbsolutePath() + "/" + mSrc.getName());
				byte[] buffer = new byte[1024];
				int len1 = 0;
				while ((len1 = mFileInputStream.read(buffer)) > 0) {
					mFileOutputStream.write(buffer, 0, len1);
				}
				mFileOutputStream.flush();
				mFileOutputStream.close();
			}

			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * copy file from src to dst
	 * 
	 * @param mSrc
	 * @param mDst
	 */
	public static boolean copyFile(File mSrc, File mDst) {
		return copyFile(mSrc, mDst, null);
	}
	
	public static boolean deleteDirectory(File path) {
		try {
			if (path.exists()) {
                File[] files = path.listFiles();
                if (files == null) {
                    return true;
                }
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
			return (path.delete());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void clearDirectory(File path) {
		try {
			if (path.exists()) {
                File[] files = path.listFiles();
                if (files == null) {
                    return ;
                }
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static String copyFile(String pathIn, String fileName, String pathOut){
			File src= new File(pathIn);
			if(!src.exists())
				return null;
			try {
				return copyFile(new FileInputStream(src), fileName, pathOut);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}
	}
	
	public static String copyFile(InputStream is, String fileName, String pathOut){
		File fileDir = new File(pathOut);
		File file = new File(fileDir, fileName);
		return copyFile(is, file.getAbsolutePath());
	}

	public static String copyFile(InputStream is, String absPath){
		try {

			BufferedInputStream inputStream = new BufferedInputStream(is);
			File file = new File(absPath);
			File fileDir = file.getParentFile();
			fileDir.mkdirs();

			FileOutputStream out = new FileOutputStream(file);

			byte buf[] = new byte[1024];
			int len;

			while ((len = inputStream.read(buf)) != -1) {
				out.write(buf, 0, len);
			}

			out.close();
			inputStream.close();

			if (file.length() == 0)
				return null;
			else
				return file.getAbsolutePath();
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean isExist(Context context,String filePath){
		if (filePath != null) {
			if (filePath.toCharArray()[0] != '/'){
				try {
					InputStream stream = context.getAssets().open(filePath);
					stream.close();
					return true;
				} catch (Exception e) {
				}
			}
			else if (filePath.contains("http"))
				return false;
			else{
				File f=new File(filePath);
				return f.exists();
			}
		}
		
		return false;
	}
	
	public static void appendFile(String data,String folder,String fileName){
		if(data ==null){
			return;
		}
		File fileFolder = new File(folder);
		if(!fileFolder.exists()){
			fileFolder.mkdirs();
		}
		
		File file = new File(folder,fileName);
		try {
			FileWriter fw=new FileWriter(file, true);
			fw.append(data+"\n");
			fw.flush();
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}
