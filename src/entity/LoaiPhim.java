package entity;
/** Danh mục loại phim trong sơ đồ lớp. */
public record LoaiPhim(String maLoaiPhim,String tenLoaiPhim,String moTa) {
 @Override public String toString(){return tenLoaiPhim;}
}
