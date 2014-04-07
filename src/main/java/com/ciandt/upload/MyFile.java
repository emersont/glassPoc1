/**
 * Class to represent a media file
 */
package com.ciandt.upload;



/**
 * @author emersont
 *
 */
public class MyFile {

        private Long id; // id of the file

        private byte[] content; // file content
        
        private String note;  // notes about the file
        
        private String name;  // the name of the file
        
        private String contentType; // the type of the file: jpg, png, gif...

		/**
		 * @return the id
		 */
		public Long getId() {
			return id;
		}

		/**
		 * @param id the id to set
		 */
		public void setId(Long id) {
			this.id = id;
		}

		/**
		 * @return the file
		 */
		public byte[] getContent() {
			return content;
		}

		/**
		 * @param file the file to set
		 */
		public void setContent(byte[] file) {
			this.content = file;
		}

		/**
		 * @return the note
		 */
		public String getNote() {
			return note;
		}

		/**
		 * @param note the note to set
		 */
		public void setNote(String note) {
			this.note = note;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return the contentType
		 */
		public String getContentType() {
			return contentType;
		}

		/**
		 * @param contentType the contentType to set
		 */
		public void setContentType(String contentType) {
			this.contentType = contentType;
		}

        
}
