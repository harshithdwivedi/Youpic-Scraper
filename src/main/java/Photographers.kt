import com.opencsv.bean.CsvBindByName

data class Photographer(
    @CsvBindByName val name: String,
    @CsvBindByName val link: String
)